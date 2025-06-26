
package com.example.androidtranscoder.format;

public class MediaFormatPresetsFactory {

    /**
     * Preset based on Nexus 4 camera recording with 720p quality.
     * This preset is ensured to work on any Android >=4.3 devices by Android CTS (if codec is available).
     * Default bitrate is 8Mbps. {@link #createVideo720pStrategy(int)} to specify bitrate.
     */
    public static MediaFormatStrategy createVideo720pStrategy() {
        return new Media720pStrategy();
    }

    /**
     * Preset based on Nexus 4 camera recording with 720p quality.
     * This preset is ensured to work on any Android >=4.3 devices by Android CTS (if codec is available).
     * Audio track will be copied as-is.
     *
     * @param bitrate Preferred bitrate for video encoding.
     */
    public static MediaFormatStrategy createVideo720pStrategy(int bitrate) {
        return new Media720pStrategy(bitrate);
    }

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

    /**
     * Preset similar to iOS SDK's AVAssetExportPreset960x540.
     * Note that encoding resolutions of this preset are not supported in all devices e.g. Nexus 4.
     * On unsupported device encoded video stream will be broken without any exception.
     */
//    public static MediaFormatStrategy createExportPreset960x540Strategy() {
//        return new Media960x540Strategy();
//    }
}
