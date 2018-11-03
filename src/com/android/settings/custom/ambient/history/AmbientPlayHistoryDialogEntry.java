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

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.android.settings.R;

import java.net.URLEncoder;

class AmbientPlayHistoryDialogEntry extends BottomSheetDialog implements View.OnClickListener {

    private AmbientPlayHistoryPreferenceEntryView entryView;
    private LinearLayout playNowButton;
    private LinearLayout searchButton;
    private LinearLayout removeButton;
    private Context mContext;
    private AmbientPlayHistoryPreferenceEntry historyEntry;

    AmbientPlayHistoryDialogEntry(Context context, AmbientPlayHistoryPreferenceEntry historyEntry) {
        super(context);
        this.mContext = context;
        this.historyEntry = historyEntry;
        View view = LayoutInflater.from(context).inflate(R.layout.ambient_play_entry_dialog, null);
        entryView = view.findViewById(R.id.entryView);
        playNowButton = view.findViewById(R.id.play);
        searchButton = view.findViewById(R.id.search);
        removeButton = view.findViewById(R.id.remove);
        playNowButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        playNowButton.setVisibility(isPlayNowAvailable() ? View.VISIBLE : View.GONE);
        searchButton.setVisibility(isSearchAvailable() ? View.VISIBLE : View.GONE);
        entryView.updateDetails(historyEntry);
        setContentView(view);
    }

    private Intent getSearchIntent(String query) {
        String escapedQuery = "";
        try {
            escapedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (Exception ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/#q=" + escapedQuery));
    }

    private boolean isSearchAvailable() {
        return getSearchIntent("").resolveActivity(mContext.getPackageManager()) != null;
    }

    private Intent getPlayNowIntent(String song, String artist) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
        intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, song);
        intent.putExtra(SearchManager.QUERY, song + " - " + artist);
        return intent;
    }

    private boolean isPlayNowAvailable() {
        return getPlayNowIntent("", "").resolveActivity(mContext.getPackageManager()) != null;
    }

    LinearLayout getRemoveButton() {
        return this.removeButton;
    }

    @Override
    public void onClick(View view) {
        if (view == playNowButton) {
            try {
                mContext.startActivity(getPlayNowIntent(historyEntry.getSongTitle(), historyEntry.getArtistTitle()));
            } catch (Exception ignored) {

            }
            dismiss();
        } else if (view == searchButton) {
            try {
                mContext.startActivity(getSearchIntent(historyEntry.getSongTitle() + " - " + historyEntry.getArtistTitle()));
            } catch (Exception ignored) {

            }
            dismiss();
        }
    }
}
