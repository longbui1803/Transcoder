
package com.example.androidtranscoder.format;

import android.media.MediaFormat;

import com.example.androidtranscoder.exception.OutputFormatUnavailableException;

public interface MediaFormatStrategy {

    /**
     * Returns preferred video format for encoding.
     *
     * @param inputFormat MediaFormat from MediaExtractor, contains csd-0/csd-1.
     * @return null for passthrough.
     * @throws OutputFormatUnavailableException if input could not be transcoded because of restrictions.
     */
    MediaFormat createVideoOutputFormat(MediaFormat inputFormat);

    /**
     * Caution: this method should return null currently.
     *
     * @return null for passthrough.
     * @throws OutputFormatUnavailableException if input could not be transcoded because of restrictions.
     */
    MediaFormat createAudioOutputFormat(MediaFormat inputFormat);

}
