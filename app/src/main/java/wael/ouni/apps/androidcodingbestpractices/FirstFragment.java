package wael.ouni.apps.androidcodingbestpractices;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import timber.log.Timber;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;

/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment {


    public FirstFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
        ImageView profileImageView = rootView.findViewById(R.id.profile_picture);

        UserWrapper userWrapper = AccountHelper.getInstance().getUserWrapper();
        Timber.i("onCreate:user : %s", userWrapper.toString());
        Glide.with(profileImageView).load(userWrapper.getPicturePath())
                .apply(fitCenterTransform()).into(profileImageView);
        return rootView;
    }

}
