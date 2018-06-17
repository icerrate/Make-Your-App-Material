package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.DateUtil;

import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_ITEM_ID = "item_id_key";

    private long mItemId;

    private Unbinder unbinder;

    @Nullable
    @BindView(R.id.meta_bar)
    LinearLayoutCompat mMetaBar;

    @BindView(R.id.photo)
    AppCompatImageView mPhotoView;

    @BindView(R.id.article_title)
    AppCompatTextView mTitleView;

    @BindView(R.id.article_byline)
    TextView mByView;

    @BindView(R.id.article_body)
    AppCompatTextView mBodyView;

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @BindView(R.id.toolbar_land)
    Toolbar mToolbarLand;

    @Nullable
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Nullable
    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (getArguments().containsKey(KEY_ITEM_ID)) {
                mItemId = getArguments().getLong(KEY_ITEM_ID);
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void bindViews(Cursor cursor) {
        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (cursor != null) {
            String title = cursor.getString(ArticleLoader.Query.TITLE);
            mTitleView.setText(title);

            String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            Date publishedDate = DateUtil.parseDate(date);
            if (!publishedDate.before(DateUtil.START_OF_EPOCH.getTime())) {
                mByView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + cursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            } else {
                // If date is before 1902, just show the string
                mByView.setText(Html.fromHtml(
                        DateUtil.parseStringDate(publishedDate) + " by <font color='#ffffff'>"
                                + cursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            }
            mBodyView.setText(Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));
            GlideApp.with(Objects.requireNonNull(getContext()))
                    .load(cursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            final Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                            if (bitmap != null) {
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    public void onGenerated(Palette p) {
                                        int mutedColor = p.getDarkMutedColor(0xFF333333);
                                        if (mToolbar != null) {
                                            mToolbar.setBackgroundColor(mutedColor);
                                        } else if (mMetaBar != null) {
                                            mMetaBar.setBackgroundColor(mutedColor);
                                        }
                                    }
                                });
                            }
                            return false;
                        }
                    })
                    .into(mPhotoView);
        } else {
            mTitleView.setText("N/A");
            mByView.setText("N/A");
            mBodyView.setText("N/A");
        }
        if (mToolbar != null) {
            setupToolbar(mToolbar);
        } else if (mToolbarLand != null) {
            setupToolbar(mToolbarLand);
        }
    }

    private void setupToolbar(Toolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onNavigateUp();
                }
            }
        });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor == null || cursor.isClosed() || !cursor.moveToFirst()) {
            return;
        }
        bindViews(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        //Empty
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.share_fab)
    public void onShareFabClicked() {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("You should try this book: " + mTitleView.getText())
                .getIntent(), getString(R.string.action_share)));
    }
}
