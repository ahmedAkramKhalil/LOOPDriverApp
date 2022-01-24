package com.general.files;

import com.taxifgo.driver.BuildConfig;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class GetFeatureClassList {

    private static String resourceFilePath = "res/layout/";
    private static String resourcePath = "layout";

    public static HashMap<String, String> getAllGeneralClasses() {
        HashMap<String, String> classParams = new HashMap<>();

        ArrayList<String> voipServiceClassList = new ArrayList<>();
        voipServiceClassList.add("com.sinch.android.rtc.SinchClient");
        voipServiceClassList.add("com.sinch.android.rtc.SinchClient");
        voipServiceClassList.add("com.general.files.SinchService");
        voipServiceClassList.add("com.general.files.SinchCallListener");
        voipServiceClassList.add("com.general.files.SinchCallClientListener");
        voipServiceClassList.add(BuildConfig.APPLICATION_ID + ".CallScreenActivity");
        voipServiceClassList.add(resourceFilePath + "callscreen");
        voipServiceClassList.add(BuildConfig.APPLICATION_ID + ".IncomingCallScreenActivity");
        voipServiceClassList.add(resourceFilePath + "incoming");

        classParams.put("VOIP_SERVICE", "No");
        for (String item : voipServiceClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("VOIP_SERVICE", "Yes");
                break;
            }
        }

        ArrayList<String> advertisementClassList = new ArrayList<>();
        advertisementClassList.add("com.general.files.OpenAdvertisementDialog");
        advertisementClassList.add(resourceFilePath + "advertisement_dailog");

        classParams.put("ADVERTISEMENT_MODULE", "No");
        for (String item : advertisementClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("ADVERTISEMENT_MODULE", "Yes");
                break;
            }
        }

        ArrayList<String> linkedInClassList = new ArrayList<>();
        linkedInClassList.add("com.general.files.OpenLinkedinDialog");
        linkedInClassList.add("com.general.files.RegisterLinkedinLoginResCallBack");

        classParams.put("LINKEDIN_MODULE", "No");
        for (String item : linkedInClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("LINKEDIN_MODULE", "Yes");
                break;
            }
        }

        ArrayList<String> cardIOClassList = new ArrayList<>();
        cardIOClassList.add("io.card.payment.CardIOActivity");

        classParams.put("CARD_IO", "No");
        for (String item : cardIOClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("CARD_IO", "Yes");
                break;
            }
        }

        ArrayList<String> liveChatClassList = new ArrayList<>();
        liveChatClassList.add("com.livechatinc.inappchat.ChatWindowActivity");

        classParams.put("LIVE_CHAT", "No");
        for (String item : liveChatClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("LIVE_CHAT", "Yes");
                break;
            }
        }

        ArrayList<String> wayBillClassList = new ArrayList<>();
        wayBillClassList.add(BuildConfig.APPLICATION_ID + ".WayBillActivity");
        wayBillClassList.add(resourceFilePath + "activity_way_bill");

        classParams.put("WAYBILL_MODULE", "No");
        for (String item : wayBillClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("WAYBILL_MODULE", "Yes");
                break;
            }
        }


        ArrayList<String> deliverAllClassList = new ArrayList<>();
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.DeliverAllCabRequestedActivity");
        deliverAllClassList.add(resourceFilePath + "activity_deliver_all_cab_requested");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.DeliverAllRatingActivity");
        deliverAllClassList.add(resourceFilePath + "activity_deliver_all_rating");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.LiveTaskListActivity");
        deliverAllClassList.add(resourceFilePath + "activity_live_tasks");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.LiveTrackOrderDetailActivity");
        deliverAllClassList.add(resourceFilePath + "activity_live_track_order_detail");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.OrderDetailsActivity");
        deliverAllClassList.add(resourceFilePath + "activity_order_details");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.OrderHistoryActivity");
        deliverAllClassList.add(resourceFilePath + "activity_order_history");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.OrderHistoryDetailActivity");
        deliverAllClassList.add(resourceFilePath + "activity_order_history_detail");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.OrderStatisticsActivity");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.PaymentCardActivity");
        deliverAllClassList.add(resourceFilePath + "activity_payment_card");
        deliverAllClassList.add(BuildConfig.APPLICATION_ID + ".deliverAll.TrackOrderActivity");
        deliverAllClassList.add(resourceFilePath + "activity_track_driver_location");
        deliverAllClassList.add("com.model.deliverAll.liveTaskListDataModel");
        deliverAllClassList.add("com.model.deliverAll.orderDetailDataModel");
        deliverAllClassList.add("com.model.deliverAll.orderItemDetailDataModel");
        deliverAllClassList.add("com.adapter.files.deliverAll.OrderHistoryRecycleAdapter");
        deliverAllClassList.add(resourceFilePath + "item_order_history_header_design");
        deliverAllClassList.add(resourceFilePath + "item_order_history_design");
        deliverAllClassList.add("com.adapter.files.deliverAll.OrderItemListRecycleAdapter");
        deliverAllClassList.add(resourceFilePath + "order_item_list_cell");
        deliverAllClassList.add("com.adapter.files.deliverAll.OrderListRecycleAdapter");
        deliverAllClassList.add(resourceFilePath + "live_task_order_list_cell");
        deliverAllClassList.add("com.adapter.files.deliverAll.VehicleSingleCheckListAdapter");
        deliverAllClassList.add(resourceFilePath + "item_select_service_deliver_all_design");

        classParams.put("DELIVER_ALL", "No");
        for (String item : deliverAllClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("DELIVER_ALL", "Yes");
                break;
            }
        }


        ArrayList<String> multiDeliveryClassList = new ArrayList<>();
        multiDeliveryClassList.add(BuildConfig.APPLICATION_ID + ".ViewMultiDeliveryDetailsActivity");
        multiDeliveryClassList.add(resourceFilePath + "activity_multi_delivery_details");
        multiDeliveryClassList.add("com.model.Delivery_Data");
        multiDeliveryClassList.add("com.model.Trip_Status");
        multiDeliveryClassList.add("com.general.files.MyScrollView");
        multiDeliveryClassList.add("com.adapter.files.MultiDeliveryDetailAdapter");
        multiDeliveryClassList.add(resourceFilePath + "multi_delivery_details_design");
        multiDeliveryClassList.add("com.adapter.files.ViewMultiDeliveryDetailRecyclerAdapter");
        multiDeliveryClassList.add(resourceFilePath + "design_view_multi_delivery_detail");

        classParams.put("MULTI_DELIVERY", "No");
        for (String item : multiDeliveryClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("MULTI_DELIVERY", "Yes");
                break;
            }
        }

        ArrayList<String> uberXClassList = new ArrayList<>();
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".AdditionalChargeActivity");
        uberXClassList.add(resourceFilePath + "activity_additional_charge");
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".AddServiceActivity");
        uberXClassList.add(resourceFilePath + "activity_add_service");
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".MoreServiceInfoActivity");
        uberXClassList.add(resourceFilePath + "activity_more_service_info");
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".MyGalleryActivity");
        uberXClassList.add(resourceFilePath + "activity_my_gallery");
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".SetAvailabilityActivity");
        uberXClassList.add(resourceFilePath + "activity_set_availability");
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".setTimeScheduleActivity");
        uberXClassList.add(resourceFilePath + "activity_set_time_schedule");
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".WorkLocationActivity");
        uberXClassList.add(resourceFilePath + "activity_work_location");
        uberXClassList.add(BuildConfig.APPLICATION_ID + ".UfxCategoryActivity");
        uberXClassList.add(resourceFilePath + "activity_ufx_category");
        uberXClassList.add("com.adapter.files.DaySlotAdapter");
        uberXClassList.add(resourceFilePath + "item_dayslot_view");
        uberXClassList.add("com.adapter.files.PinnedCategorySectionListAdapter");
        uberXClassList.add(resourceFilePath + "category_section_list_item");
        uberXClassList.add("com.adapter.files.GalleryImagesRecyclerAdapter");
        uberXClassList.add(resourceFilePath + "item_gallery_list");
        uberXClassList.add("com.adapter.files.OnGoingTripDetailAdapter");
        uberXClassList.add(resourceFilePath + "item_design_ongoing_trip_cell");
        uberXClassList.add("com.adapter.files.TimeSlotAdapter");
        uberXClassList.add(resourceFilePath + "item_timeslot_view");

        classParams.put("UBERX_SERVICE", "No");
        for (String item : uberXClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                Logger.e("EXIST_FILE", "::" + item);
                classParams.put("UBERX_SERVICE", "Yes");
                break;
            }
        }

        ArrayList<String> newsClassList = new ArrayList<>();
        newsClassList.add(BuildConfig.APPLICATION_ID + ".NotificationActivity");
        newsClassList.add(resourceFilePath + "activity_notification");
        newsClassList.add(BuildConfig.APPLICATION_ID + ".NotificationDetailsActivity");
        newsClassList.add(resourceFilePath + "activity_notification_details");
        newsClassList.add("com.fragments.NotiFicationFragment");
        newsClassList.add(resourceFilePath + "fragment_notification");
        newsClassList.add("com.adapter.files.NotificationAdapter");
        newsClassList.add(resourceFilePath + "item_notification_view");

        classParams.put("NEWS_SECTION", "No");
        for (String item : newsClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("NEWS_SECTION", "Yes");
                break;
            }
        }

        ArrayList<String> rentalClassList = new ArrayList<>();
        rentalClassList.add(BuildConfig.APPLICATION_ID + ".RentalDetailsActivity");
        rentalClassList.add(resourceFilePath + "activity_rental_details");
        rentalClassList.add(BuildConfig.APPLICATION_ID + ".RentalInfoActivity");
        rentalClassList.add(resourceFilePath + "activity_rental_info");
        rentalClassList.add("com.adapter.files.PackageAdapter");
        rentalClassList.add(resourceFilePath + "item_package_row");

        classParams.put("RENTAL_FEATURE", "No");
        for (String item : rentalClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("RENTAL_FEATURE", "Yes");
                break;
            }
        }


        ArrayList<String> deliveryModuleClassList = new ArrayList<>();
        deliveryModuleClassList.add(BuildConfig.APPLICATION_ID + ".ViewDeliveryDetailsActivity");
        deliveryModuleClassList.add(resourceFilePath + "activity_view_delivery_details");

        classParams.put("DELIVERY_MODULE", "No");
        for (String item : deliveryModuleClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("DELIVERY_MODULE", "Yes");
                break;
            }
        }


        ArrayList<String> rideModuleClassList = new ArrayList<>();
        rideModuleClassList.add(BuildConfig.APPLICATION_ID + ".HailActivity");
        rideModuleClassList.add(resourceFilePath + "activity_hail");
        rideModuleClassList.add("com.fragments.CabSelectionFragment");
        rideModuleClassList.add(resourceFilePath + "fragment_new_cab_selection");
        rideModuleClassList.add("com.adapter.files.CabTypeAdapter");
        rideModuleClassList.add(resourceFilePath + "item_design_cab_type");

        classParams.put("RIDE_SECTION", "No");
        for (String item : rideModuleClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("RIDE_SECTION", "Yes");
                break;
            }
        }

        ArrayList<String> rduModuleClassList = new ArrayList<>();
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".AddAddressActivity");
        rduModuleClassList.add(resourceFilePath + "activity_add_address");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".ActiveTripActivity");
        rduModuleClassList.add(resourceFilePath + "activity_active_trip");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".AddAddressActivity");
        rduModuleClassList.add(resourceFilePath + "activity_add_address");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".CabRequestedActivity");
        rduModuleClassList.add(resourceFilePath + "activity_cab_requested");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".ChatActivity");
        rduModuleClassList.add(resourceFilePath + "design_trip_chat_detail_dialog");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".CollectPaymentActivity");
        rduModuleClassList.add(resourceFilePath + "activity_collect_payment");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".ConfirmEmergencyTapActivity");
        rduModuleClassList.add(resourceFilePath + "activity_confirm_emergency_tap");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".DriverArrivedActivity");
        rduModuleClassList.add(resourceFilePath + "activity_driver_arrived");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".EmergencyContactActivity");
        rduModuleClassList.add(resourceFilePath + "activity_emergency_contact");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".FareBreakDownActivity");
        rduModuleClassList.add(resourceFilePath + "activity_fare_break_down");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".HistoryActivity");
        rduModuleClassList.add(resourceFilePath + "activity_history");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".MyBookingsActivity");
        rduModuleClassList.add(resourceFilePath + "activity_my_bookings");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".MyHeatViewActivity");
        rduModuleClassList.add(resourceFilePath + "activity_heatview");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".PrefranceActivity");
        rduModuleClassList.add(resourceFilePath + "activity_prefrance");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".RideHistoryActivity");
        rduModuleClassList.add(resourceFilePath + "activity_ride_history");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".RideHistoryDetailActivity");
        rduModuleClassList.add(resourceFilePath + "activity_ride_history_detail");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".SearchPickupLocationActivity");
        rduModuleClassList.add(resourceFilePath + "activity_search_pickup_location");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".SearchPickupLocationActivity");
        rduModuleClassList.add(resourceFilePath + "activity_search_pickup_location");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".SearchLocationActivity");
        rduModuleClassList.add(resourceFilePath + "activity_search_location");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".SelectedDayHistoryActivity");
        rduModuleClassList.add(resourceFilePath + "activity_selected_day_history");
        rduModuleClassList.add(BuildConfig.APPLICATION_ID + ".TripRatingActivity");
        rduModuleClassList.add(resourceFilePath + "activity_trip_rating");
        rduModuleClassList.add("com.fragments.BookingFragment");
        rduModuleClassList.add(resourceFilePath + "fragment_booking");
        rduModuleClassList.add("com.fragments.DeliveryFragment");
        rduModuleClassList.add(resourceFilePath + "fragment_delivery");
        rduModuleClassList.add("com.fragments.RideHistoryFragment");
        rduModuleClassList.add(resourceFilePath + "activity_ride_history");
        rduModuleClassList.add("com.fragments.MainHeaderFragment");
        rduModuleClassList.add(resourceFilePath + "fragment_main_header");
        rduModuleClassList.add("com.adapter.files.ChatMessage");
        rduModuleClassList.add("com.adapter.files.ChatMessagesRecycleAdapter");
        rduModuleClassList.add(resourceFilePath + "message");
        rduModuleClassList.add("com.adapter.files.CustSpinnerAdapter");
        rduModuleClassList.add(resourceFilePath + "item_spinnertextview");
        rduModuleClassList.add("com.adapter.files.EmergencyContactRecycleAdapter");
        rduModuleClassList.add(resourceFilePath + "emergency_contact_item");
        rduModuleClassList.add("com.adapter.files.MyBookingsRecycleAdapter");
        rduModuleClassList.add(resourceFilePath + "item_my_bookings_design");
        rduModuleClassList.add("com.general.files.OpenPassengerDetailDialog");
        rduModuleClassList.add(resourceFilePath + "design_passenger_detail_dialog");
        rduModuleClassList.add("com.general.files.CancelTripDialog");
        rduModuleClassList.add(resourceFilePath + "decline_order_dialog_design");
        rduModuleClassList.add("com.general.files.OpenUserInstructionDialog");
        rduModuleClassList.add(resourceFilePath + "design_user_instruction_dialog");
        rduModuleClassList.add("com.adapter.files.CategoryListItem");

        classParams.put("RDU_SECTION", "No");
        for (String item : rduModuleClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("RDU_SECTION", "Yes");
                break;
            }
        }

        ArrayList<String> tollModuleClassList = new ArrayList<>();
        tollModuleClassList.add("com.utils.CommonUtilities.TOLLURL");

        classParams.put("TOLL_MODULE", "No");

        Class<?> commonUtilsClz = CommonUtilities.class;
        try {
            Field field_chk = commonUtilsClz.getField("TOLLURL");
            if (field_chk != null) {
                classParams.put("TOLL_MODULE", "Yes");
            } else {
                classParams.put("TOLL_MODULE", "No");
            }
        } catch (Exception ex) {
            classParams.put("TOLL_MODULE", "No");
        }

        ArrayList<String> endOfDayModuleClassList = new ArrayList<>();
        endOfDayModuleClassList.add(BuildConfig.APPLICATION_ID + ".FavouriteDriverActivity");
        endOfDayModuleClassList.add("com.adapter.files.RecentLocationAdpater");
        endOfDayModuleClassList.add(resourceFilePath + "design_end_day_start_trip");
        endOfDayModuleClassList.add(resourceFilePath + "item_recent_loc_design");

        classParams.put("END_OF_DAY_TRIP_SECTION", "No");
        for (String item : endOfDayModuleClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("END_OF_DAY_TRIP_SECTION", "Yes");
                break;
            }
        }

        ArrayList<String> stopOverPointModuleClassList = new ArrayList<>();
        stopOverPointModuleClassList.add("com.adapter.files.ViewStopOverDetailRecyclerAdapter");
        stopOverPointModuleClassList.add(BuildConfig.APPLICATION_ID + ".ViewStopOverDetailsActivity");
        stopOverPointModuleClassList.add(resourceFilePath + "activity_stop_over_details");
        stopOverPointModuleClassList.add(resourceFilePath + "design_view_stop_over_detail");

        classParams.put("STOP_OVER_POINT_SECTION", "No");
        for (String item : stopOverPointModuleClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("STOP_OVER_POINT_SECTION", "Yes");
                break;
            }
        }

        ArrayList<String> driverSubscriptionModuleClassList = new ArrayList<>();
        driverSubscriptionModuleClassList.add("com.adapter.files.SubscriptionAdapter");
        driverSubscriptionModuleClassList.add(resourceFilePath + "item_subscription_history");
        driverSubscriptionModuleClassList.add(resourceFilePath + "item_subscription");
        driverSubscriptionModuleClassList.add(BuildConfig.APPLICATION_ID + ".SubscribedPlanConfirmationActivity");
        driverSubscriptionModuleClassList.add(resourceFilePath + "activity_subscription_purchased");
        driverSubscriptionModuleClassList.add(BuildConfig.APPLICATION_ID + ".SubscriptionActivity");
        driverSubscriptionModuleClassList.add(resourceFilePath + "activity_subscription");
        driverSubscriptionModuleClassList.add(BuildConfig.APPLICATION_ID + ".SubscriptionHistoryActivity");
        driverSubscriptionModuleClassList.add(resourceFilePath + "activity_subscription");
        driverSubscriptionModuleClassList.add(BuildConfig.APPLICATION_ID + ".SubscriptionPaymentActivity");
        driverSubscriptionModuleClassList.add(resourceFilePath + "subcription_payment_activity");

        classParams.put("DRIVER_SUBSCRIPTION_SECTION", "No");
        for (String item : driverSubscriptionModuleClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("DRIVER_SUBSCRIPTION_SECTION", "Yes");
                break;
            }
        }


        ArrayList<String> goPayModuleClassList = new ArrayList<>();
        goPayModuleClassList.add(resourceFilePath + "design_transfer_money");

        classParams.put("GO_PAY_SECTION", "No");
        for (String item : goPayModuleClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("GO_PAY_SECTION", "Yes");
                break;
            }
        }

        ArrayList<String> flyClassList = new ArrayList<>();
        flyClassList.add("com.adapter.files.SkyPortsRecyclerAdapter");
        flyClassList.add(resourceFilePath + "design_choose_skyports");
        flyClassList.add("com.fragments.FlyStationSelectionFragment");
        flyClassList.add(resourceFilePath + "design_skyports_bottom_view");
        classParams.put("FLY_MODULE", "No");
        for (String item : flyClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("FLY_MODULE", "Yes");
                break;
            }
        }


        /** Removal file of libraries **/
        voipServiceClassList.add("libs/sinch_lib.aar");
        voipServiceClassList.add("Libs folder remove file called 'sinch_lib' Or any lib which is related to SINCH");
        cardIOClassList.add("Go to App's Level build.Gradle File and Remove Library 'io.card:android-sdk'");
        liveChatClassList.add("Go to App's Level build.Gradle File and Remove Library 'com.github.livechat:chat-window-android'");
        tollModuleClassList.add("Remove Declaration of Toll URL from CommonUtilities File And remove portion of Toll cost from code. (Remove Network execution of toll URL)");
        /** Removal file of libraries **/

        if (classParams.get("WAYBILL_MODULE") != null && classParams.get("WAYBILL_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("WAYBILL_MODULE_FILES", android.text.TextUtils.join(",", wayBillClassList));
        }

        if (classParams.get("VOIP_SERVICE") != null && classParams.get("VOIP_SERVICE").equalsIgnoreCase("Yes")) {
            classParams.put("VOIP_SERVICE_FILES", android.text.TextUtils.join(",", voipServiceClassList));
        }

        if (classParams.get("ADVERTISEMENT_MODULE") != null && classParams.get("ADVERTISEMENT_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("ADVERTISEMENT_MODULE_FILES", android.text.TextUtils.join(",", advertisementClassList));
        }

        if (classParams.get("LINKEDIN_MODULE") != null && classParams.get("LINKEDIN_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("LINKEDIN_MODULE_FILES", android.text.TextUtils.join(",", linkedInClassList));
        }

        if (classParams.get("CARD_IO") != null && classParams.get("CARD_IO").equalsIgnoreCase("Yes")) {
            classParams.put("CARD_IO_FILES", android.text.TextUtils.join(",", cardIOClassList));
        }

        if (classParams.get("LIVE_CHAT") != null && classParams.get("LIVE_CHAT").equalsIgnoreCase("Yes")) {
            classParams.put("LIVE_CHAT_FILES", android.text.TextUtils.join(",", liveChatClassList));
        }

        if (classParams.get("DELIVER_ALL") != null && classParams.get("DELIVER_ALL").equalsIgnoreCase("Yes")) {
            classParams.put("DELIVER_ALL_FILES", android.text.TextUtils.join(",", deliverAllClassList));
        }

        if (classParams.get("MULTI_DELIVERY") != null && classParams.get("MULTI_DELIVERY").equalsIgnoreCase("Yes")) {
            classParams.put("MULTI_DELIVERY_FILES", android.text.TextUtils.join(",", multiDeliveryClassList));
        }

        if (classParams.get("UBERX_SERVICE") != null && classParams.get("UBERX_SERVICE").equalsIgnoreCase("Yes")) {
            classParams.put("UBERX_FILES", android.text.TextUtils.join(",", uberXClassList));
        }

        if (classParams.get("NEWS_SECTION") != null && classParams.get("NEWS_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("NEWS_SERVICE_FILES", android.text.TextUtils.join(",", newsClassList));
        }

        if (classParams.get("RENTAL_FEATURE") != null && classParams.get("RENTAL_FEATURE").equalsIgnoreCase("Yes")) {
            classParams.put("RENTAL_SERVICE_FILES", android.text.TextUtils.join(",", rentalClassList));
        }

        if (classParams.get("DELIVERY_MODULE") != null && classParams.get("DELIVERY_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("DELIVERY_MODULE_FILES", android.text.TextUtils.join(",", deliveryModuleClassList));
        }

        if (classParams.get("RIDE_SECTION") != null && classParams.get("RIDE_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("RIDE_SECTION_FILES", android.text.TextUtils.join(",", rideModuleClassList));
        }

        if (classParams.get("RDU_SECTION") != null && classParams.get("RDU_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("RDU_SECTION_FILES", android.text.TextUtils.join(",", rduModuleClassList));
        }

        if (classParams.get("TOLL_MODULE") != null && classParams.get("TOLL_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("TOLL_MODULE_FILES", android.text.TextUtils.join(",", tollModuleClassList));
        }

        if (classParams.get("STOP_OVER_POINT_SECTION") != null && classParams.get("STOP_OVER_POINT_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("STOP_OVER_POINT_SECTION_FILES", android.text.TextUtils.join(",", stopOverPointModuleClassList));
        }

        if (classParams.get("END_OF_DAY_TRIP_SECTION") != null && classParams.get("END_OF_DAY_TRIP_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("END_OF_DAY_TRIP_SECTION_FILES", android.text.TextUtils.join(",", endOfDayModuleClassList));
        }

        if (classParams.get("DRIVER_SUBSCRIPTION_SECTION") != null && classParams.get("DRIVER_SUBSCRIPTION_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("DRIVER_SUBSCRIPTION_SECTION_FILES", android.text.TextUtils.join(",", driverSubscriptionModuleClassList));
        }

        if (classParams.get("GO_PAY_SECTION") != null && classParams.get("GO_PAY_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("GO_PAY_SECTION_FILES", android.text.TextUtils.join(",", goPayModuleClassList));
        }

        if (classParams.get("FLY_MODULE") != null && classParams.get("FLY_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("FLY_MODULE_FILES", android.text.TextUtils.join(",", flyClassList));
        }

        classParams.put("PACKAGE_NAME", BuildConfig.APPLICATION_ID);

        return classParams;
    }
}
