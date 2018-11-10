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
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.android.settings.custom.ambient.history.utils.TimeDateUtils;

import com.android.settings.R;

class AmbientPlayHistoryPreferenceEntry extends Preference {
    private Context mContext;
    private int id;
    private long ts;
    private String song;
    private String artist;
    private PreferenceCategory preferenceCategory;

    AmbientPlayHistoryPreferenceEntry(int id, long ts, String song, String artist, Context context) {
        super(context);
        mContext = context;
        this.id = id;
        this.ts = ts;
        this.song = song;
        this.artist = artist;
        setTitle(getSongTitle());
        setSummary(getArtistTitle() + " • " + getFormattedMatchTime());
        setIcon(R.drawable.ic_music_note);
    }

    void removePreference() {
        if (this.preferenceCategory != null) {
            this.preferenceCategory.removePreference(this);
        }
    }

    PreferenceCategory getPreferenceCategory() {
        return this.preferenceCategory;
    }

    void setPreferenceCategory(PreferenceCategory preferenceCategory) {
        this.preferenceCategory = preferenceCategory;
    }

    String getFormattedSummary() {
        String formattedSongAndArtist = String.format(mContext.getString(R.string.ambient_play_recognition_information), getSongTitle(), getArtistTitle());
        String time = TimeDateUtils.tsToLocalTime(mContext, this.ts);
        String daysAgo = TimeDateUtils.getDaysAgo(mContext, this.ts, false);
        return formattedSongAndArtist + " • " + (daysAgo.equals(mContext.getString(R.string.ambient_play_history_today)) ? time : daysAgo);
    }

    String getFormattedMatchTime() {
        return TimeDateUtils.tsToLocalTime(mContext, this.ts);
    }

    String getFormattedMatchDate() {
        return TimeDateUtils.tsToLocalDate(mContext, this.ts);
    }

    long geMatchTimestamp() {
        return this.ts;
    }

    int getSongID() {
        return this.id;
    }

    String getArtistTitle() {
        return this.artist;
    }

    String getSongTitle() {
        return this.song;
    }

}
