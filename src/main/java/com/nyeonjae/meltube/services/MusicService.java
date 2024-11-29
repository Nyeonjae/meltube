package com.nyeonjae.meltube.services;

import com.nyeonjae.meltube.entities.MusicEntity;
import com.nyeonjae.meltube.entities.UserEntity;
import com.nyeonjae.meltube.exceptions.TransactionalException;
import com.nyeonjae.meltube.mappers.MusicMapper;
import com.nyeonjae.meltube.results.CommonResult;
import com.nyeonjae.meltube.results.Result;
import com.nyeonjae.meltube.results.music.AddMusicResult;
import com.nyeonjae.meltube.vos.ResultVo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MusicService {
    private final MusicMapper musicMapper;

    @Autowired
    public MusicService(MusicMapper musicMapper) {
        this.musicMapper = musicMapper;
    }

    public Result addMusic(UserEntity user, MusicEntity music, MultipartFile cover) throws IOException, InterruptedException {
        if (user == null || user.isSuspended() || user.getDeletedAt() != null) {
            return CommonResult.FAILURE_UNSIGNED;
        }
        if (music == null ||
                music.getArtist() == null || music.getArtist().isEmpty() || music.getArtist().length() > 50 ||
                music.getAlbum() == null || music.getAlbum().isEmpty() || music.getAlbum().length() > 50 ||
                music.getReleaseDate() == null ||
                music.getName() == null || music.getName().isEmpty() || music.getName().length() > 50 ||
                music.getYoutubeId() == null || music.getYoutubeId().length() != 11 ||
                (music.getCoverFileName() == null && cover == null)) {
            // music 의 coverFileName 도 null 이고 cover 도 null 이면 커버 이미지를 아예 안줬다는 말이니까
            return CommonResult.FAILURE;
        }
        if (this.musicMapper.selectMusicByYoutubeId(music.getYoutubeId()) != null) {
            return AddMusicResult.FAILURE_DUPLICATE_YOUTUBE_ID;
        }

        if (cover == null) {
            // 멜론 검색으로 이미지를 첨부 했으면 발생하는 로직
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(music.getCoverFileName()))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                return CommonResult.FAILURE;
            }
            music.setCoverData(response.body());
            music.setCoverContentType("image/jpeg");
            music.setCoverFileName("_cover.jpg");
        } else {
            // 직접 커버 이미지를 첨부 했으면 발생하는 로직
            music.setCoverData(cover.getBytes());
            music.setCoverContentType(cover.getContentType());
            music.setCoverFileName(cover.getOriginalFilename());
        }
        music.setUserEmail(user.getEmail());
        music.setStatus(MusicEntity.Status.PENDING.name()); // "PENDING"
        music.setCreatedAt(LocalDateTime.now());
        music.setUpdatedAt(null);
        music.setDeleted(false);
        return this.musicMapper.insertMusic(music) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    public MusicEntity crawlMelon(String id) throws IOException {
        if (id == null || id.isEmpty()) {
            return null;
        }
        String url = String.format("https://www.melon.com/song/detail.htm?songId=%s", id);
        Document document = Jsoup.connect(url).get();
        Elements $name = document.select(".song_name");
        if ($name.isEmpty()) {
            return null;
        }
        $name.select(".none").remove();
        Elements $artist = document.select(".artist_name > span:first-child");
        Elements $list = document.select("dl.list");
        Elements $album = $list.select("dd:nth-child(2)");
        Elements $release = $list.select("dd:nth-child(4)");
        Elements $genre = $list.select("dd:nth-child(6)");
        Elements $lyrics = document.select(".lyric");
        Elements $cover = document.select("img[src^=\"https://cdnimg.melon.co.kr/cm2/album/images/\"]");

        String releaseDate = $release.text().replace(".", "-");
        if (releaseDate.split("-").length == 2) {
            // 가령 ["1989" , "12"] 임으로
            releaseDate += "-01";
            // "1989-12-01" 로 만들고
        }
        MusicEntity music = new MusicEntity();
        music.setArtist($artist.text());
        music.setAlbum($album.text());
        music.setReleaseDate(LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        music.setGenre($genre.text());
        music.setName($name.text());
        music.setLyrics($lyrics.text());
        music.setCoverFileName($cover.attr("src"));
        // 여기까진 음악 정보를 불러옴

        String youtubeId = null;
        try {
//            String searchQuery = URLEncoder.encode(String.format("%s %s site:youtube.com", music.getArtist(), music.getName()), StandardCharsets.UTF_8);
//            Document googleSearchResult = Jsoup.connect(String.format("https://www.google.com/search?q=%s", searchQuery)).get();
//            Element $firstH3 = googleSearchResult.selectFirst("h3");
//            if ($firstH3 != null) {
//                Element $anchor = $firstH3.parent();
//                if ($anchor != null) {
//                    String href = $anchor.attr("href");
//                    String[] hrefArray = href.split("=");
//                    if (hrefArray.length > 1) {
//                        youtubeId = href.split("=")[1];
//                    }
//                }
//            }
            String searchQuery = URLEncoder.encode(String.format("%s %s", music.getArtist(), music.getName()), StandardCharsets.UTF_8);
            Document naverSearchResul = Jsoup.connect(String.format("https://search.naver.com/search.naver?where=video&sort=rel&view=big&query=%s&selected_cp=3130781782", searchQuery)).get();
            Element $firstAnchor = naverSearchResul.selectFirst("a.info_title");
            if ($firstAnchor != null) {
                String href = $firstAnchor.attr("href");
                String[] hrefArray = href.split("=");
                if (hrefArray.length > 1) {
                    youtubeId = hrefArray[1];
                }
            }
        } catch (HttpStatusException ignored) {
            youtubeId = null;

        }
        music.setYoutubeId(youtubeId);

        return music;
    }


    @Transactional
    public Result withdrawInquiries(UserEntity user, int[] indexes) {
        if (user == null || user.isSuspended() || user.getDeletedAt() != null) {
            return CommonResult.FAILURE_UNSIGNED;
        }
        if (indexes == null || indexes.length == 0) {
            return CommonResult.FAILURE;
        }

        for (int index : indexes) {
            MusicEntity music = this.musicMapper.selectMusicByIndex(index, false);
            if (music == null || music.isDeleted()) {
                throw new TransactionalException();
            }
            if (!music.getUserEmail().equals(user.getEmail())) {
                throw new TransactionalException();
            }
            if (!music.getStatus().equals(MusicEntity.Status.PENDING.name())) {
                throw new TransactionalException();
            }
            music.setDeleted(true);
            if (this.musicMapper.updateMusic(music, false) == 0 ) {
                throw new TransactionalException();
            }
        }
        return CommonResult.SUCCESS;
    }


    public MusicEntity getMusicByIndex(Integer index, boolean includeCover) {
        if (index == null || index < 1) {
            return null;
        }
        return this.musicMapper.selectMusicByIndex(index, includeCover);
    }


    public ResultVo<Result, MusicEntity[]> getMusicInquiriesByUser(UserEntity user) {
        if (user == null || user.isSuspended() || user.getDeletedAt() != null) {
            return ResultVo.<Result, MusicEntity[]>builder().result(CommonResult.FAILURE_UNSIGNED).build();
        }
        return ResultVo.<Result, MusicEntity[]>builder()
                .result(CommonResult.SUCCESS)
                .payload(this.musicMapper.selectMusicByUserEmail(user.getEmail()))
                .build();
    }


    public MusicEntity[] searchMelon(String keyword) throws IOException, InterruptedException {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        keyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = String.format("https://www.melon.com/search/keyword/index.json?query=%s", keyword);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        String responseText = response.body();
        JSONObject responseObject = new JSONObject(responseText);
        if (!responseObject.has("SONGCONTENTS")) {
            return new MusicEntity[0];
        }
        JSONArray songArray = responseObject.getJSONArray("SONGCONTENTS");
        MusicEntity[] musicArray = new MusicEntity[songArray.length()];

        for (int i = 0; i < songArray.length(); i++) {
            JSONObject song = songArray.getJSONObject(i);

            MusicEntity music = new MusicEntity();
            music.setArtist(song.getString("ARTISTNAME"));
            music.setAlbum(song.getString("ALBUMNAME"));
            music.setName(song.getString("SONGNAME"));
            music.setCoverFileName(song.getString("ALBUMIMG"));
            music.setYoutubeId(song.getString("SONGID"));
            musicArray[i] = music;
        }

        return musicArray;
    }

    public boolean verifyYoutubeId(String id) throws IOException, InterruptedException {
        if (id == null || id.length() != 11) {
            return false;
        }
        String url = String.format("http://img.youtube.com/vi/%s/mqdefault.jpg", id);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode() != 404;
    }
}
