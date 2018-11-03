/*
 * Copyright (C) 2018 PixelExperience
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.android.settings.custom.ambient.history;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.settings.custom.ambient.history.utils.TimeDateUtils;

import com.android.settings.R;

public class AmbientPlayHistoryPreferenceEntryView extends LinearLayout {
    private Context mContext;

    public AmbientPlayHistoryPreferenceEntryView(Context context) {
        super(context);
        mContext = context;
        inflate(context, R.layout.ambient_play_entry_view, this);
        setVisibility(View.GONE);
    }

    public AmbientPlayHistoryPreferenceEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        inflate(context, R.layout.ambient_play_entry_view, this);
        setVisibility(View.GONE);
    }

    public AmbientPlayHistoryPreferenceEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        inflate(context, R.layout.ambient_play_entry_view, this);
        setVisibility(View.GONE);
    }

    public AmbientPlayHistoryPreferenceEntryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        inflate(context, R.layout.ambient_play_entry_view, this);
        setVisibility(View.GONE);
    }

    public void updateDetails(AmbientPlayHistoryPreferenceEntry entry) {
        RelativeLayout layout = findViewById(R.id.relative_layout);
        TextView songView = layout.findViewById(R.id.song);
        TextView artistView = layout.findViewById(R.id.artist);
        String time = TimeDateUtils.tsToLocalTime(mContext, entry.geMatchTimestamp());
        songView.setText(entry.getSongTitle());
        artistView.setText(String.format("%s • %s", entry.getArtistTitle(), time));
        setVisibility(View.VISIBLE);
    }
}
