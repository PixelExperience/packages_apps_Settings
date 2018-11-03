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
import android.preference.Preference;
import android.util.AttributeSet;

import com.android.internal.util.ambient.play.AmbientPlayHistoryEntry;
import com.android.internal.util.ambient.play.AmbientPlayHistoryManager;

import com.android.settings.R;

import java.util.List;

public class AmbientPlayHistoryPreference extends Preference implements Preference.OnPreferenceClickListener {
    public AmbientPlayHistoryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        updateSummary(context);
    }

    public AmbientPlayHistoryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        updateSummary(context);
    }

    public AmbientPlayHistoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        updateSummary(context);
    }

    public AmbientPlayHistoryPreference(Context context) {
        super(context);
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        updateSummary(context);
    }

    private void updateSummary(Context context) {
        List<AmbientPlayHistoryEntry> songs = AmbientPlayHistoryManager.getSongs(context);
        if (songs.size() < 1) {
            setSummary(R.string.ambient_play_history_empty);
        } else {
            AmbientPlayHistoryEntry entry = songs.get(0);
            AmbientPlayHistoryPreferenceEntry preference = new AmbientPlayHistoryPreferenceEntry(entry.getSongID(), entry.geMatchTimestamp(), entry.getSongTitle(), entry.getArtistTitle(), context);
            setSummary(preference.getFormattedSummary());
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
}
