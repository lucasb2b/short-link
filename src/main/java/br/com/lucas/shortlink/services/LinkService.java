package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.entities.User;
import br.com.lucas.shortlink.exceptions.*;
import br.com.lucas.shortlink.repositories.LinkRepository;
import br.com.lucas.shortlink.repositories.UserRepository;
import br.com.lucas.shortlink.utils.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public LinkService(
            LinkRepository linkRepository,
            UserRepository userRepository
    ){
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
    }

    public Link createShortLink(String originalUrl, String userEmail){

        validateUrl(originalUrl);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        String shortCode;
        boolean exists;

        do{
            shortCode = ShortCodeGenerator.generate(6);
            exists = linkRepository.existsByShortCode(shortCode);
        }while (exists);

        Link link = Link.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .shortUrl(baseUrl + "/" + shortCode)
                .user(user)
                .build();

        return linkRepository.save(link);
    }

    public String getOriginalUrlByCode(String shortCode) {
        return linkRepository.findByShortCode(shortCode)
                .map(Link::getOriginalUrl)
                .orElse(null);
    }


    private void validateUrl(String url){
        try{
            URI uri = new URI(url);

            String scheme = uri.getScheme();

            if(scheme == null){
                throw new InvalidUrlException("URL inválida");
            }

            if(
                    !scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https")
            ){
                throw new InvalidUrlException("Protocolo não permitido");
            }
        } catch (URISyntaxException e){
            throw new InvalidUrlException("URL inválida");
        }
    }

    /**
     * @Cacheable verifica se existe a chave "links::shortCode" no Redis.
     * Se existir, nem bate no banco de dados.
     * Se não existir, executa o método e salva o retorno no Redis.
     */
    @Cacheable(value = "links", key = "#shortCode")
    public Link findByShortCode(String shortCode){

        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(LinkNotFoundException::new);

        if(link.isRevoked()){
            throw new LinkRevokedException();
        }

        return link;
    }



    /**
     * @CacheEvict limpa o cache do Redis para este link.
     * Assim, garantimos que se o link for revogado, o cache é atualizado (removido) imediatamente.
     */
    @CacheEvict(value = "links", key = "#shortCode")
    public void revokeLink(String shortCode, String userEmail){

        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(LinkNotFoundException::new);

        if(!link.getUser().getEmail().equals(userEmail)){
            throw new UnauthorizedException();
        }

        link.setRevoked(true);

        linkRepository.save(link);
    }

    public Page<Link> getUserLinks(String userEmail, int page) {
        // Cria a paginação: página atual, tamanho 10, ordenado do mais recente para o mais antigo
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        return linkRepository.findByUserEmail(userEmail, pageable);
    }

}
