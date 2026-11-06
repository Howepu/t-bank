package backend.academy.scrapper.services.jdbc;

import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.Tag;
import backend.academy.scrapper.repositories.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.repositories.jdbc.JdbcTagRepository;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class JdbcLinkService implements LinkService {

    private final JdbcLinkRepository linkRepository;
    private final ChatService chatService;
    private final JdbcTagRepository tagRepository;

    @Override
    public Link addLink(Long chatId, Link link) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        linkRepository.addLink(link, chatId);

        return link;
    }

    @Override
    public Link removeLink(Long chatId, String url) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        if (chatId == null || url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Chat ID и URL должны быть предоставлены и не могут быть пустыми");
        }

        // Поиск записи по URL
        Link link = linkRepository.findByUrl(url);
        if (link == null) {
            throw new IllegalArgumentException("Ссылка не найдена для удаления");
        }

        // Логируем перед удалением
        log.info("Удаление ссылки с URL: {}", link.url());

        // Удаляем запись
        linkRepository.deleteByUrl(url);

        // Логируем после удаления
        log.info("Ссылка с URL: {} успешно удалена", url);

        return link;
    }

    @Override
    public List<Link> getAllLinks(Long chatId) {

        return linkRepository.findAllLinksByChatId(chatId);
    }

    @Override
    public void updateLastUpdated(Long id, LocalDateTime createdAt) {
        linkRepository.updateCreatedAt(id, createdAt);
    }

    @Override
    public List<Link> findLinksByTag(String tagName, Long chatId) {
        return linkRepository.findLinksByTag(tagName, chatId);
    }

    @Override
    public void attachTagToLink(String url, String name) {
        Link link = linkRepository.findByUrl(url);
        Tag tag = tagRepository.getByName(name);
        linkRepository.attachTagToLink(link.id(), tag.id());
    }

    @Override
    public void detachTagFromLink(String url, String name) {
        Link link = linkRepository.findByUrl(url);
        Tag tag = tagRepository.getByName(name);
        linkRepository.detachTagFromLink(link.id(), tag.id());
    }

    @Override
    public void updateLinkIssues(Long linkId, List<Long> issueIds) {
        linkRepository.updateLinkIssues(linkId, issueIds);
    }

    @Override
    public void updateLinkPullRequests(Long linkId, List<Long> prIds) {
        linkRepository.updateLinkPullRequests(linkId, prIds);
    }

    @Override
    public List<String> getFilterNamesByLink(Long linkId) {
        return linkRepository.getFilterNameByLink(linkId);
    }
}
