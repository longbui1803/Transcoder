
package com.example.androidtranscoder.format;

public class MediaFormatPresetsFactory {
    /**
     * Preset based on Nexus 4 camera recording with 720p quality.
     * This preset is ensured to work on any Android >=4.3 devices by Android CTS (if codec is available).
     * <br>
     * Note: audio transcoding is experimental feature.
     *
     * @param bitrate       Preferred bitrate for video encoding.
     * @param audioBitrate  Preferred bitrate for audio encoding.
     * @param audioChannels Output audio channels.
     */
    public static MediaFormatStrategy createVideo720pStrategy(int bitrate, int audioBitrate, int audioChannels) {
        return new Media720pStrategy(bitrate, audioBitrate, audioChannels);
    }

}
