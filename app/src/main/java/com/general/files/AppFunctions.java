package com.general.files;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;

import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Xml;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.taxifgo.driver.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.NavigationSensor;
import com.utils.Utils;
import com.view.SelectableRoundedImageView;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

public class AppFunctions {
    Context mContext;
    GeneralFunctions generalFunc;

    public AppFunctions(Context mContext) {
        this.mContext = mContext;
        generalFunc = new GeneralFunctions(mContext);
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, String userProfileJson, String imageKey) {
        String vImgName_str = generalFunc.getJsonValue(imageKey, userProfileJson);

        Picasso.get().load(CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, JSONObject userProfileJsonObj, String imageKey, ImageView profilebackimage) {
        String vImgName_str = generalFunc.getJsonValueStr(imageKey, userProfileJsonObj);

        Picasso.get().load(CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);

        Picasso.get().load(CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                Utils.setBlurImage(bitmap, profilebackimage);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public CameraUpdate getCameraPosition(Location location, GoogleMap gMap) {
        //if (isFirst) {
        double ZoomLevel = Utils.defaultZomLevel;
        // }

        float bearing = NavigationSensor.getInstance().getCurrentBearing();

        Logger.e("CAMERA_BEARING", "::"+bearing);
        CameraPosition cameraPosition = null;
        if (gMap == null) {
            cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .bearing(bearing != -1 ? bearing : 0)
                    .zoom((float) ZoomLevel)
                    .build();

        } else {
            cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .bearing(bearing != -1 ? bearing : gMap.getCameraPosition().bearing)
                    .zoom((float) ZoomLevel)
                    .build();
        }

        if (cameraPosition == null) {
            return null;
        }

        return (CameraUpdateFactory.newCameraPosition(cameraPosition));


    }


    public boolean checkSinchInstance(SinchService.SinchServiceInterface sinchServiceInterface) {
        boolean isNull = sinchServiceInterface != null && sinchServiceInterface.getSinchClient() != null;
        Logger.d("call", "Instance" + isNull);
        return isNull;
    }

    public static AttributeSet getXmlResource(Context mContext, int resourceId){
        XmlPullParser parser = mContext.getResources().getXml(resourceId);
        try {
            parser.next();
            parser.nextTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AttributeSet attr = Xml.asAttributeSet(parser);

        return attr;

//        int count = attr.getAttributeCount();

//        final XmlResourceParser parser = mContext.getResources().getLayout(resourceId);
//        AttributeSet attr = Xml.asAttributeSet(parser);
        /*try {
            return attr;
        } finally {
            parser.close();
        }*/
    }

    public void setOverflowButtonColor(final Toolbar toolbar, final int color) {
        Drawable drawable = toolbar.getOverflowIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), color);
            toolbar.setOverflowIcon(drawable);
        }
    }

    public static Spanned fromHtml(String html){
        if(!Utils.checkText(html)){
            // return an empty spannable if the html is null
            return new SpannableString("");
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }
}
