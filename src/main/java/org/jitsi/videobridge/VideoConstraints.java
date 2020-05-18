/*
 * Copyright @ 2017 - Present, 8x8 Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.videobridge;

import org.jitsi.videobridge.cc.config.*;

import java.util.*;

/**
 * Expresses the ideal video constraints for an endpoint. We may wish to support
 * track-based constraints in the future.
 */
public class VideoConstraints
{
    /**
     * A constraints object for the given endpoint id, with ideal height set to
     * 180p.
     *
     * Pinned endpoints are those that we always want to have in the last-n
     * set in LD, if they're not on-stage, or in HD if they're on-stage
     * (provided that there's enough bandwidth, but that's up to the bitrate
     * controller to decide).
     *
     * Note that a selected endpoint can be pinned. Signaling that to the
     * bridge may sound a bit redundant, after all if an endpoint is
     * selected, we already have a 720p constraint for it. However, when the
     * selected endpoint goes off-stage, it needs to maintain its status
     * as "pinned".
     *
     * By setting the ideal height to 180, a receiver expresses the "desire"
     * to watch them in low resolution. This will result in being
     * prioritized during the bandwidth allocation step.
     */
    public static final VideoConstraints PINNED_ENDPOINT_CONSTRAINT
        = new VideoConstraints(BitrateControllerConfig.Config.thumbnailMaxHeightPx());

    /**
     * A constraints object for the given endpoint id, with ideal height set to
     * 720p.
     *
     *  Pinned endpoints are those that we want to see in HD because they're
     *  (provided that there's enough bandwidth, but that's up to the bitrate
     *  controller to decide).
     *
     *  By setting the ideal height to 720, a receiver expresses the "desire"
     *  to watch them in high resolution. This will result in being
     *  prioritized during the bandwidth allocation step.
     */
    public static final VideoConstraints
        SELECTED_ENDPOINT_CONSTRAINT = new VideoConstraints(720,
        BitrateControllerConfig.Config.onstagePreferredHeightPx(),
        BitrateControllerConfig.Config.onstagePreferredFramerate());

    public static final VideoConstraints EMPTY = new VideoConstraints(-1);

    /**
     * The ideal height of the constrained endpoint. We try to send an encoding
     * that matches this resolution as close as possible, if bandwidth is
     * available.
     */
    private final int idealHeight;

    private final int preferredHeight;

    public int getPreferredHeight()
    {
        return preferredHeight;
    }

    public double getPreferredFps()
    {
        return preferredFps;
    }

    private final double preferredFps;

    /**
     * Ctor.
     *
     * @param idealHeight The ideal height of the constrained endpoint.
     */
    VideoConstraints(int idealHeight, int preferredHeight, double preferredFps)
    {
        this.preferredFps = preferredFps;
        this.preferredHeight = preferredHeight;
        this.idealHeight = idealHeight;
    }

    /**
     * Ctor.
     *
     * @param idealHeight The ideal height of the constrained endpoint.
     */
    VideoConstraints(int idealHeight)
    {
        this(idealHeight, -1, -1);
    }

    /**
     * The max height constrained was added for tile-view back when everything
     * was expressed as "selected" and "pinned" endpoints, the idea being we
     * mark everything as selected (so endpoints aren't limited to 180p) and
     * set the max to 360p (so endpoints are limited to 360p, instead of 720p
     * which is normally used for selected endpoints. This was the quickest, not
     * the nicest way to implement the tile-view constraints signaling and it
     * was subsequently used to implement low-bandwidth mode.
     *
     * Now, one negative side effect, other than being a hack, was that for
     * selected endpoints, we eagerly allocate bandwidth up to 360p30fps. This
     * eager bandwidth allocation was something we had discussed and agreed
     * upon several moons ago, but it doesn't work well in tile-view because we
     * end-up with a lot of ninjas.
     *
     * By simply setting an ideal height X as a global constraint, we signal to
     * the bitrate controller that it needs to (evenly) distribute bandwidth
     * across all participants, up to X.
     *
     * @param idealHeight the ideal height of the constraint object.
     *
     * @return a constraints object without endpoint id (can be used as a global
     * endpoint constraint), with ideal height set to the given ideal height.
     */
    static VideoConstraints makeMaxHeightVideoConstraints(int idealHeight)
    {
        return new VideoConstraints(idealHeight);
    }

    public int getIdealHeight()
    {
        return idealHeight;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoConstraints that = (VideoConstraints) o;
        return idealHeight == that.idealHeight;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(idealHeight);
    }
}
