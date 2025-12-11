package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.dto.music.BookPlaylistResponse;
import BukkeuBukkeu.Book_Eum.service.music.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/music")
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping("/playlist/{isbn}")
    public BookPlaylistResponse getBookPlaylist(@PathVariable String isbn) {
        return playlistService.buildBookPlaylist(isbn);
    }
}
