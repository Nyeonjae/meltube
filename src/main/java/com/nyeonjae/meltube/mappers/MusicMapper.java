package com.nyeonjae.meltube.mappers;

import com.nyeonjae.meltube.entities.MusicEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MusicMapper {
    int insertMusic(MusicEntity music);

    MusicEntity selectMusicByIndex(@Param("index") int index,
                                   @Param("includeCover") boolean includeCover);


    MusicEntity selectMusicByYoutubeId(@Param("youtubeId") String youtubeId);


    MusicEntity[] selectMusics(@Param("includeCover") boolean includeCover);

    MusicEntity[] selectMusicByUserEmail(@Param("userEmail") String userEmail);

    int updateMusic(@Param("music") MusicEntity music,
                    @Param("includeCover") boolean includeCover
                    );


}
