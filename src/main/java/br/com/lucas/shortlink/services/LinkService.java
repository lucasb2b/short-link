package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.entities.User;
import br.com.lucas.shortlink.repositories.LinkRepository;
import br.com.lucas.shortlink.repositories.UserRepository;
import br.com.lucas.shortlink.utils.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkService {

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private UserRepository userRepository;

    public Link createLink(String originalUrl, String userEmail){

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String shortCode;
        boolean exists;

        do{
            shortCode = ShortCodeGenerator.generate(6);
            exists = linkRepository.existsByShortCode(shortCode);
        }while (exists);

        Link link = Link.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .shortUrl("http://localhost:8080/" + shortCode)
                .user(user)
                .build();

        return linkRepository.save(link);
    }

    public String getOriginalUrlByCode(String shortCode) {
        return linkRepository.findByShortCode(shortCode)
                .map(Link::getOriginalUrl)
                .orElse(null);
    }

    public Link findByShortCode(String shortCode){
        return linkRepository.findByShortCode(shortCode).orElse(null);
    }

}
