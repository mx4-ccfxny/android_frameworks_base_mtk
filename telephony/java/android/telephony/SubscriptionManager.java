/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.telephony;

import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;
import android.telephony.Rlog;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.RemoteException;

import com.android.internal.telephony.ISub;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * SubscriptionManager is the application interface to SubscriptionController
 * and provides information about the current Telephony Subscriptions.
 *
 * The android.Manifest.permission.READ_PHONE_STATE to retrieve the information, except
 * getActiveSubIdList and getActiveSubIdCount for which no permission is needed.
 *
 * @hide - to be unhidden
 */
public class SubscriptionManager implements BaseColumns {
    private static final String LOG_TAG = "SUB";
    private static final boolean DBG = true;
    private static final boolean VDBG = false;

    /** An invalid subscription identifier */
    /** {@hide} */
    public static final int INVALID_SUBSCRIPTION_ID = -1;

    /** Base value for Dummy SUBSCRIPTION_ID's. */
    /** FIXME: Remove DummySubId's, but for now have them map just below INVALID_SUBSCRIPTION_ID
    /** @hide */
    public static final int DUMMY_SUBSCRIPTION_ID_BASE = INVALID_SUBSCRIPTION_ID - 1;

    /** An invalid phone identifier */
    /** @hide */
    public static final int INVALID_PHONE_INDEX = -1;

    /** An invalid slot identifier */
    /** @hide */
    public static final int INVALID_SIM_SLOT_INDEX = -1;

    /** Indicates the caller wants the default sub id. */
    /** @hide */
    public static final int DEFAULT_SUBSCRIPTION_ID = Integer.MAX_VALUE;

    /**
     * Indicates the caller wants the default phone id.
     * Used in SubscriptionController and PhoneBase but do we really need it???
     * @hide
     */
    public static final int DEFAULT_PHONE_INDEX = Integer.MAX_VALUE;

    /** Indicates the caller wants the default slot id. NOT used remove? */
    /** @hide */
    public static final int DEFAULT_SIM_SLOT_INDEX = Integer.MAX_VALUE;

    /** Minimum possible subid that represents a subscription */
    /** @hide */
    public static final int MIN_SUBSCRIPTION_ID_VALUE = 0;

    /** Maximum possible subid that represents a subscription */
    /** @hide */
    public static final int MAX_SUBSCRIPTION_ID_VALUE = DEFAULT_SUBSCRIPTION_ID - 1;

    /** An invalid phone identifier */
    /** @hide - to be unhidden */
    public static final int INVALID_PHONE_ID = -1000;

    /** Indicates the caller wants the default phone id. */
    /** @hide - to be unhidden */
    public static final int DEFAULT_PHONE_ID = Integer.MAX_VALUE;

    /** An invalid slot identifier */
    /** @hide - to be unhidden */
    public static final int INVALID_SLOT_ID = -1000;

    /** Indicates the caller wants the default slot id. */
    /** @hide */
    public static final int DEFAULT_SLOT_ID = Integer.MAX_VALUE;

    /** Indicates the user should be asked which sub to use. */
    /** @hide */
    public static final int ASK_USER_SUB_ID = -1001;

    /** An invalid subscription identifier */
    public static final int INVALID_SUB_ID = INVALID_SUBSCRIPTION_ID;

    /** Indicates the caller wants the default sub id. */
    /** @hide - to be unhidden */
    public static final int DEFAULT_SUB_ID = Integer.MAX_VALUE;

    /** @hide */
    public static final Uri CONTENT_URI = Uri.parse("content://telephony/siminfo");

    /**
     * TelephonyProvider unique key column name is the subscription id.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String UNIQUE_KEY_SUBSCRIPTION_ID = "_id";

    /** @hide */
    public static final int DEFAULT_INT_VALUE = -100;

    /** @hide */
    public static final String DEFAULT_STRING_VALUE = "N/A";

    /** @hide */
    public static final int EXTRA_VALUE_NEW_SIM = 1;

    /** @hide */
    public static final int EXTRA_VALUE_REMOVE_SIM = 2;
    /** @hide */
    public static final int EXTRA_VALUE_REPOSITION_SIM = 3;
    /** @hide */
    public static final int EXTRA_VALUE_NOCHANGE = 4;

    /** @hide */
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    /** @hide */
    public static final String INTENT_KEY_SIM_COUNT = "simCount";
    /** @hide */
    public static final String INTENT_KEY_NEW_SIM_SLOT = "newSIMSlot";
    /** @hide */
    public static final String INTENT_KEY_NEW_SIM_STATUS = "newSIMStatus";

    /**
     * The ICC ID of a SIM.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String ICC_ID = "icc_id";

    /**
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String SIM_ID = "sim_id";

    /**
     * TelephonyProvider column name for user SIM_SlOT_INDEX
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String SIM_SLOT_INDEX = "sim_id";

    /** SIM is not inserted */
    /** @hide - to be unhidden */
    public static final int SIM_NOT_INSERTED = -1;

    /**
     * The Network mode of SIM/sub.
     * <P>Type: INTEGER (int)</P>
     * {@hide}
     */
    public static final String NETWORK_MODE = "network_mode";

    /**
     * The user configured Network mode of SIM/sub.
     * <P>Type: INTEGER (int)</P>
     * {@hide}
     */
    public static final String USER_NETWORK_MODE = "user_network_mode";

    /** {@hide} */
    public static final int DEFAULT_NW_MODE = -1;

    /**
     * The activation state of SIM/sub.
     * <P>Type: INTEGER (int)</P>
     * {@hide}
     */
    public static final String SUB_STATE = "sub_state";

    /** {@hide} */
    public static final int INACTIVE = 0;
    /** {@hide} */
    public static final int ACTIVE = 1;
    /** {@hide} */
    public static final int SUB_CONFIGURATION_IN_PROGRESS = 2;

    /**
     * TelephonyProvider column name for user displayed name.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String DISPLAY_NAME = "display_name";

    /**
     * TelephonyProvider column name for the service provider name for the SIM.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String CARRIER_NAME = "carrier_name";

    /**
     * Default name resource
     * @hide
     */
    public static final int DEFAULT_NAME_RES = com.android.internal.R.string.unknownName;

    /**
     * TelephonyProvider column name for source of the user displayed name.
     * <P>Type: INT (int)</P> with one of the NAME_SOURCE_XXXX values below
     *
     * @hide
     */
    public static final String NAME_SOURCE = "name_source";

    /**
     * The name_source is undefined
     * @hide
     */
    public static final int NAME_SOURCE_UNDEFINDED = -1;

    /**
     * The name_source is the default
     * @hide
     */
    public static final int NAME_SOURCE_DEFAULT_SOURCE = 0;

    /**
     * The name_source is from the SIM
     * @hide
     */
    public static final int NAME_SOURCE_SIM_SOURCE = 1;

    /**
     * The name_source is from the user
     * @hide
     */
    public static final int NAME_SOURCE_USER_INPUT = 2;

    /**
     * TelephonyProvider column name for the color of a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String COLOR = "color";

    /** @hide */
    public static final int COLOR_1 = 0;

    /** @hide */
    public static final int COLOR_2 = 1;

    /** @hide */
    public static final int COLOR_3 = 2;

    /** @hide */
    public static final int COLOR_4 = 3;

    /** @hide */
    public static final int COLOR_DEFAULT = COLOR_1;

    /**
     * TelephonyProvider column name for the phone number of a SIM.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String NUMBER = "number";

    /**
     * TelephonyProvider column name for the number display format of a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String DISPLAY_NUMBER_FORMAT = "display_number_format";

    /** @hide */
    public static final int DISPLAY_NUMBER_NONE = 0;

    /** @hide */
    public static final int DISPLAY_NUMBER_FIRST = 1;

    /** @hide */
    public static final int DISPLAY_NUMBER_LAST = 2;

    /** @hide */
    public static final int DISLPAY_NUMBER_DEFAULT = DISPLAY_NUMBER_FIRST;

    /**
     * TelephonyProvider column name for permission for data roaming of a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String DATA_ROAMING = "data_roaming";

    /** @hide */
    public static final int DATA_ROAMING_ENABLE = 1;

    /** @hide */
    public static final int DATA_ROAMING_DISABLE = 0;

    /** @hide */
    public static final int DATA_ROAMING_DEFAULT = DATA_ROAMING_DISABLE;

    /**
     * TelephonyProvider column name for the MCC associated with a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    public static final String MCC = "mcc";

    /**
     * TelephonyProvider column name for the MNC associated with a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    public static final String MNC = "mnc";


    private static final int RES_TYPE_BACKGROUND_DARK = 0;

    private static final int RES_TYPE_BACKGROUND_LIGHT = 1;

    private static final int[] sSimBackgroundDarkRes = setSimResource(RES_TYPE_BACKGROUND_DARK);

    public static final String SUB_PREFIX = "SUB 0";

    /**
     * Broadcast Action: The user has changed one of the default subs related to
     * data, phone calls, or sms</p>
     * @hide
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String SUB_DEFAULT_CHANGED_ACTION =
        "android.intent.action.SUB_DEFAULT_CHANGED";

    private final Context mContext;

    /**
     * A listener class for monitoring changes to {@link SubscriptionInfo} records.
     * <p>
     * Override the onSubscriptionsChanged method in the object that extends this
     * class and pass it to {@link #addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener)}
     * to register your listener and to unregister invoke
     * {@link #removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener)}
     * <p>
     * Permissions android.Manifest.permission.READ_PHONE_STATE is required
     * for #onSubscriptionsChanged to be invoked.
     */
    public static class OnSubscriptionsChangedListener {
        /** @hide */
        public static final String PERMISSION_ON_SUBSCRIPTIONS_CHANGED =
                android.Manifest.permission.READ_PHONE_STATE;

        private final Handler mHandler  = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (DBG) {
                    log("handleMessage: invoke the overriden onSubscriptionsChanged()");
                }
                OnSubscriptionsChangedListener.this.onSubscriptionsChanged();
            }
        };

        /**
         * Callback invoked when there is any change to any SubscriptionInfo. Typically
         * this method would invoke {@link #getActiveSubscriptionInfoList}
         */
        public void onSubscriptionsChanged() {
            if (DBG) log("onSubscriptionsChanged: NOT OVERRIDDEN");
        }

        /**
         * The callback methods need to be called on the handler thread where
         * this object was created.  If the binder did that for us it'd be nice.
         */
        IOnSubscriptionsChangedListener callback = new IOnSubscriptionsChangedListener.Stub() {
            @Override
            public void onSubscriptionsChanged() {
                if (DBG) log("callback: received, sendEmptyMessage(0) to handler");
                mHandler.sendEmptyMessage(0);
            }
            @Override
            public void onUnregistered() {
                mHandler.removeMessages(0);
            }
        };

        private void log(String s) {
            Rlog.d(LOG_TAG, s);
        }
	}

    /** @hide */
    public SubscriptionManager() {
		this(null);
		logd("MTK compatibility ctor");
    }

	/** @hide */
	public SubscriptionManager(final Context ctx) {
        if (DBG) logd("SubscriptionManager created");
		mContext = ctx;
	}

    /**
     * Get an instance of the SubscriptionManager from the Context.
     * This invokes {@link android.content.Context#getSystemService
     * Context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)}.
     *
     * @param context to use.
     * @return SubscriptionManager instance
     */
    public static SubscriptionManager from(Context context) {
        return (SubscriptionManager) context.getSystemService(
                Context.TELEPHONY_SUBSCRIPTION_SERVICE);
    }
    /**
     * Register for changes to the list of active {@link SubscriptionInfo} records or to the
     * individual records themselves. When a change occurs the onSubscriptionsChanged method of
     * the listener will be invoked immediately if there has been a notification.
     *
     * @param listener an instance of {@link OnSubscriptionsChangedListener} with
     *                 onSubscriptionsChanged overridden.
     */
    public void addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = mContext != null ? mContext.getPackageName() : "<unknown>";
        if (DBG) {
            logd("register OnSubscriptionsChangedListener pkgForDebug=" + pkgForDebug
                    + " listener=" + listener);
        }
        try {
            // We use the TelephonyRegistry as it runs in the system and thus is always
            // available. Where as SubscriptionController could crash and not be available
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                    "telephony.registry"));
            if (tr != null) {
                tr.addOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException ex) {
            // Should not happen
        }
    }

    /**
     * Unregister the {@link OnSubscriptionsChangedListener}. This is not strictly necessary
     * as the listener will automatically be unregistered if an attempt to invoke the listener
     * fails.
     *
     * @param listener that is to be unregistered.
     */
    public void removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = mContext != null ? mContext.getPackageName() : "<unknown>";
        if (DBG) {
            logd("unregister OnSubscriptionsChangedListener pkgForDebug=" + pkgForDebug
                    + " listener=" + listener);
        }
        try {
            // We use the TelephonyRegistry as its runs in the system and thus is always
            // available where as SubscriptionController could crash and not be available
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                    "telephony.registry"));
            if (tr != null) {
                tr.removeOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException ex) {
            // Should not happen
		}
	}

    /**
     * Get the SubInfoRecord associated with the subId
     * @param subId The unique SubInfoRecord index in database
     * @return SubInfoRecord, maybe null
     * @hide - to be unhidden
     */
    public static SubInfoRecord getSubInfoForSubscriber(int subId) {
        if (!isValidSubId(subId)) {
            logd("[getSubInfoForSubscriberx]- invalid subId, subId = " + subId);
            return null;
        }

        SubInfoRecord subInfo = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subInfo = iSub.getSubInfoForSubscriber(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subInfo;

    }

    /**
     * Get the SubInfoRecord according to an IccId
     * @param iccId the IccId of SIM card
     * @return SubInfoRecord List, maybe empty but not null
     * @hide
     */
    public static List<SubInfoRecord> getSubInfoUsingIccId(String iccId) {
        if (VDBG) logd("[getSubInfoUsingIccId]+ iccId=" + iccId);
        if (iccId == null) {
            logd("[getSubInfoUsingIccId]- null iccid");
            return null;
        }

        List<SubInfoRecord> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSubInfoUsingIccId(iccId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }


        if (result == null) {
            result = new ArrayList<SubInfoRecord>();
        }
        return result;
    }

    /**
     * Get the SubInfoRecord according to slotId
     * @param slotId the slot which the SIM is inserted
     * @return SubInfoRecord list, maybe empty but not null
     * @hide - to be unhidden
     */
    public static List<SubInfoRecord> getSubInfoUsingSlotId(int slotId) {
        // FIXME: Consider never returning null
        if (!isValidSlotId(slotId)) {
            logd("[getSubInfoUsingSlotId]- invalid slotId, slotId = " + slotId);
            return null;
        }

        List<SubInfoRecord> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSubInfoUsingSlotId(slotId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }


        if (result == null) {
            result = new ArrayList<SubInfoRecord>();
        }
        return result;
    }

    /**
     * Get all the SubInfoRecord(s) in subInfo database
     * @return List of all SubInfoRecords in database, include those that were inserted before
     * maybe empty but not null.
     * @hide
     */
    public static List<SubInfoRecord> getAllSubInfoListMTK() {
        if (VDBG) logd("[getAllSubInfoList]+");

        List<SubInfoRecord> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoListMTK();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (result == null) {
            result = new ArrayList<SubInfoRecord>();
        }
        return result;
    }

    /**
     * Get the SubInfoRecord(s) of the currently inserted SIM(s)
     * @return Array list of currently inserted SubInfoRecord(s) maybe empty but not null
     * @hide - to be unhidden
     */
    public static List<SubInfoRecord> getActiveSubInfoList() {
        List<SubInfoRecord> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoList();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (result == null) {
            result = new ArrayList<SubInfoRecord>();
        }
        return result;
    }

    /**
     * Get the active SubscriptionInfo with the subId key
     * @param subId The unique SubscriptionInfo key in database
     * @return SubscriptionInfo, maybe null if its not active.
     */
    public SubscriptionInfo getActiveSubscriptionInfo(int subId) {
        if (VDBG) logd("[getActiveSubscriptionInfo]+ subId=" + subId);
        if (!isValidSubscriptionId(subId)) {
            logd("[getActiveSubscriptionInfo]- invalid subId");
            return null;
        }

        SubscriptionInfo subInfo = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subInfo = iSub.getActiveSubscriptionInfo(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subInfo;

    }

    /**
     * Get the active SubscriptionInfo associated with the iccId
     * @param iccId the IccId of SIM card
     * @return SubscriptionInfo, maybe null if its not active
     * @hide
     */
    public SubscriptionInfo getActiveSubscriptionInfoForIccIndex(String iccId) {
        if (VDBG) logd("[getActiveSubscriptionInfoForIccIndex]+ iccId=" + iccId);
        if (iccId == null) {
            logd("[getActiveSubscriptionInfoForIccIndex]- null iccid");
            return null;
        }

        SubscriptionInfo result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoForIccId(iccId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Get the active SubscriptionInfo associated with the slotIdx
     * @param slotIdx the slot which the subscription is inserted
     * @return SubscriptionInfo, maybe null if its not active
     */
    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx) {
        if (VDBG) logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIdx=" + slotIdx);
        if (!isValidSlotId(slotIdx)) {
            logd("[getActiveSubscriptionInfoForSimSlotIndex]- invalid slotIdx");
            return null;
        }

        SubscriptionInfo result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoForSimSlotIndex(slotIdx);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * @return List of all SubscriptionInfo records in database,
     * include those that were inserted before, maybe empty but not null.
     * @hide
     */
    public List<SubscriptionInfo> getAllSubscriptionInfoList() {
        if (VDBG) logd("[getAllSubscriptionInfoList]+");

        List<SubscriptionInfo> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoList();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (result == null) {
            result = new ArrayList<SubscriptionInfo>();
        }
        return result;
    }

    /**
     * Get the SubscriptionInfo(s) of the currently inserted SIM(s). The records will be sorted
     * by {@link SubscriptionInfo#getSimSlotIndex} then by {@link SubscriptionInfo#getSubscriptionId}.
     *
     * @return Sorted list of the currently {@link SubscriptionInfo} records available on the device.
     * <ul>
     * <li>
     * If null is returned the current state is unknown but if a {@link OnSubscriptionsChangedListener}
     * has been registered {@link OnSubscriptionsChangedListener#onSubscriptionsChanged} will be
     * invoked in the future.
     * </li>
     * <li>
     * If the list is empty then there are no {@link SubscriptionInfo} records currently available.
     * </li>
     * <li>
     * if the list is non-empty the list is sorted by {@link SubscriptionInfo#getSimSlotIndex}
     * then by {@link SubscriptionInfo#getSubscriptionId}.
     * </li>
     * </ul>
     */
    public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
        List<SubscriptionInfo> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoList();
            }
        } catch (RemoteException ex) {
            // ignore it
        }
        return result;
    }

    /**
     * Get the SUB count of all SUB(s) in subinfo database
     * @return all SIM count in database, include what was inserted before
     * @hide
     */
    public static int getAllSubInfoCount() {
        if (VDBG) logd("[getAllSubInfoCount]+");

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoCount();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Get the count of active SUB(s)
     * @return active SIM count
     * @hide
     */
    public static int getActiveSubInfoCount() {
        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoCount();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * @return the count of all subscriptions in the database, this includes
     * all subscriptions that have been seen.
     * @hide
     */
    public int getAllSubscriptionInfoCount() {
        if (VDBG) logd("[getAllSubscriptionInfoCount]+");

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoCount();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Get the count of activated SUB(s)
     * @return the current number of active subscriptions. There is no guarantee the value
     * returned by this method will be the same as the length of the list returned by
     * {@link #getActiveSubscriptionInfoList}.
     */
    public int getActiveSubscriptionInfoCount() {
        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoCount();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * @return the maximum number of active subscriptions that will be returned by
     * {@link #getActiveSubscriptionInfoList} and the value returned by
     * {@link #getActiveSubscriptionInfoCount}.
     */
    public int getActiveSubscriptionInfoCountMax() {
        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoCountMax();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Add a new SubInfoRecord to subinfo database if needed
     * @param iccId the IccId of the SIM card
     * @param slotId the slot which the SIM is inserted
     * @return the URL of the newly created row or the updated row
     * @hide
     */
    public static Uri addSubInfoRecord(String iccId, int slotId) {
        if (VDBG) logd("[addSubInfoRecord]+ iccId:" + iccId + " slotId:" + slotId);
        if (iccId == null) {
            logd("[addSubInfoRecord]- null iccId");
        }
        if (!isValidSlotId(slotId)) {
            logd("[addSubInfoRecord]- invalid slotId, slotId = " + slotId);
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                // FIXME: This returns 1 on success, 0 on error should should we return it?
                iSub.addSubInfoRecord(iccId, slotId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        // FIXME: Always returns null?
        return null;

    }

    /**
     * Add a new SubscriptionInfo to SubscriptionInfo database if needed
     * @param iccId the IccId of the SIM card
     * @param slotId the slot which the SIM is inserted
     * @return the URL of the newly created row or the updated row
     * @hide
     */
    public Uri addSubscriptionInfoRecord(String iccId, int slotId) {
        if (VDBG) logd("[addSubscriptionInfoRecord]+ iccId:" + iccId + " slotId:" + slotId);
        if (iccId == null) {
            logd("[addSubscriptionInfoRecord]- null iccId");
        }
        if (!isValidSlotId(slotId)) {
            logd("[addSubscriptionInfoRecord]- invalid slotId");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                // FIXME: This returns 1 on success, 0 on error should should we return it?
                iSub.addSubInfoRecord(iccId, slotId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        // FIXME: Always returns null?
        return null;

    }

    /**
     * Set SIM color by simInfo index
     * @param color the color of the SIM
     * @param subId the unique SubInfoRecord index in database
     * @return the number of records updated
     * @hide
     */
    public static int setColor(int color, int subId) {
        if (VDBG) logd("[setColor]+ color:" + color + " subId:" + subId);
        int size = sSimBackgroundDarkRes.length;
        if (!isValidSubId(subId) || color < 0 || color >= size) {
            logd("[setColor]- fail, subId = " + subId + ", color = " + color);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setColor(color, subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /**
     * Set display name by simInfo index
     * @param displayName the display name of SIM card
     * @param subId the unique SubInfoRecord index in database
     * @return the number of records updated
     * @hide
     */
    public static int setDisplayName(String displayName, int subId) {
        return setDisplayName(displayName, subId, NAME_SOURCE_UNDEFINDED);
    }

    /**
     * Set display name by simInfo index with name source
     * @param displayName the display name of SIM card
     * @param subId the unique SubInfoRecord index in database
     * @param nameSource 0: NAME_SOURCE_DEFAULT_SOURCE, 1: NAME_SOURCE_SIM_SOURCE,
     *                   2: NAME_SOURCE_USER_INPUT, -1 NAME_SOURCE_UNDEFINED
     * @return the number of records updated or -1 if invalid subId
     * @hide
     */
    public static int setDisplayName(String displayName, int subId, int nameSource) {
        if (VDBG) {
            logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId
                    + " nameSource:" + nameSource);
        }
        if (!isValidSubId(subId)) {
            logd("[setDisplayName]- fail, subId = " + subId);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDisplayNameUsingSrc(displayName, subId, nameSource);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /**
     * Set phone number by subId
     * @param number the phone number of the SIM
     * @param subId the unique SubInfoRecord index in database
     * @return the number of records updated
     * @hide
     */
    public static int setDisplayNumber(String number, int subId) {
        if (number == null || !isValidSubId(subId)) {
            logd("[setDisplayNumber]- fail, subId = " + subId);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDisplayNumber(number, subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /**
     * Set number display format. 0: none, 1: the first four digits, 2: the last four digits
     * @param format the display format of phone number
     * @param subId the unique SubInfoRecord index in database
     * @return the number of records updated
     * @hide
     */
    public static int setDisplayNumberFormat(int format, int subId) {
        if (VDBG) logd("[setDisplayNumberFormat]+ format:" + format + " subId:" + subId);
        if (format < 0 || !isValidSubId(subId)) {
            logd("[setDisplayNumberFormat]- fail, return -1, subId = " + subId);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDisplayNumberFormat(format, subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /**
     * Set data roaming by simInfo index
     * @param roaming 0:Don't allow data when roaming, 1:Allow data when roaming
     * @param subId the unique SubInfoRecord index in database
     * @return the number of records updated
     * @hide
     */
    public static int setDataRoaming(int roaming, int subId) {
        if (VDBG) logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        if (roaming < 0 || !isValidSubId(subId)) {
            logd("[setDataRoaming]- fail, subId = " + subId);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDataRoaming(roaming, subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Get slotId associated with the subscription.
     * @return slotId as a positive integer or a negative value if an error either
     * SIM_NOT_INSERTED or INVALID_SLOT_ID.
     * @hide - to be unhidden
     */
    public static int getSlotId(int subId) {
        if (!isValidSubId(subId)) {
            logd("[getSlotId]- fail, subId = " + subId);
        }

        int result = INVALID_SLOT_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSlotId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /** @hide */
    public static int[] getSubId(int slotId) {
        return getSubIdUsingSlotId(slotId);
    }

    public static int[] getSubIdUsingSlotId(int slotId) {
        if (VDBG) logd("[getSubIdUsingSlotId]+ slotId:" + slotId);

        if (!isValidSlotId(slotId)) {
            logd("[getSubIdUsingSlotId]- fail, slotId = " + slotId);
            return null;
        }

        int[] subId = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getSubIdUsingSlotId(slotId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subId;
    }

    public static int getSubIdUsingPhoneId(int phoneId) {
        if (VDBG) logd("[getSubIdUsingPhoneId]+ phoneId:" + phoneId);

        int subId = INVALID_SUB_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getSubIdUsingPhoneId(phoneId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subId;
    }

    /** @hide */
    public static int getPhoneId(int subId) {
        if (!isValidSubId(subId)) {
            logd("[getPhoneId]- fail, subId = " + subId);
            return INVALID_PHONE_ID;
        }

        int result = INVALID_PHONE_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getPhoneId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("[getPhoneId]- phoneId=" + result);
        return result;

    }

    private static int[] setSimResource(int type) {
        int[] simResource = null;

        switch (type) {
            case RES_TYPE_BACKGROUND_DARK:
                simResource = new int[] {
                    com.android.internal.R.drawable.sim_dark_blue,
                    com.android.internal.R.drawable.sim_dark_orange,
                    com.android.internal.R.drawable.sim_dark_green,
                    com.android.internal.R.drawable.sim_dark_purple
                };
                break;
            case RES_TYPE_BACKGROUND_LIGHT:
                simResource = new int[] {
                    com.android.internal.R.drawable.sim_light_blue,
                    com.android.internal.R.drawable.sim_light_orange,
                    com.android.internal.R.drawable.sim_light_green,
                    com.android.internal.R.drawable.sim_light_purple
                };
                break;
        }

        return simResource;
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, "[SubManager] " + msg);
    }

    public static void setDefaultSubId(int subId) {
        if (VDBG) logd("setDefaultSubId sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /**
     * @return the "system" defaultSubId on a voice capable device this
     * will be getDefaultVoiceSubId() and on a data only device it will be
     * getDefaultDataSubId().
     * @hide
     */
    public static int getDefaultSubId() {
        int subId = INVALID_SUB_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public static int getDefaultVoiceSubId() {
        int subId = INVALID_SUB_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultVoiceSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultVoiceSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public static void setDefaultVoiceSubId(int subId) {
        if (VDBG) logd("setDefaultVoiceSubId sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultVoiceSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultVoiceSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /** @hide */
    public static SubInfoRecord getDefaultVoiceSubInfo() {
        return getSubInfoForSubscriber(getDefaultVoiceSubId());
    }

    /** @hide */
    public static int getDefaultVoicePhoneId() {
        return getPhoneId(getDefaultVoiceSubId());
    }

    /**
     * @return subId of the DefaultSms subscription or the value INVALID_SUB_ID if an error.
     * @hide - to be unhidden
     */
    public static int getDefaultSmsSubId() {
        int subId = INVALID_SUB_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSmsSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultSmsSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public static void setDefaultSmsSubId(int subId) {
        if (VDBG) logd("setDefaultSmsSubId sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultSmsSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultSmsSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /** @hide */
    public static SubInfoRecord getDefaultSmsSubInfo() {
        return getSubInfoForSubscriber(getDefaultSmsSubId());
    }

    /** @hide */
    public static int getDefaultSmsPhoneId() {
        return getPhoneId(getDefaultSmsSubId());
    }

    /** @hide */
    public static int getDefaultDataSubId() {
        int subId = INVALID_SUB_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultDataSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultDataSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public static void setDefaultDataSubId(int subId) {
        if (VDBG) logd("setDataSubscription sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultDataSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /** @hide */
    public static SubInfoRecord getDefaultDataSubInfo() {
        return getSubInfoForSubscriber(getDefaultDataSubId());
    }

    /** @hide */
    public static int getDefaultDataPhoneId() {
        return getPhoneId(getDefaultDataSubId());
    }

    /** @hide */
    public static void clearSubInfo() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearSubInfo();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return;
    }

    //FIXME this is vulnerable to race conditions
    /** @hide */
    public static boolean allDefaultsSelected() {
        if (getDefaultDataSubId() == INVALID_SUB_ID) {
            return false;
        }
        if (getDefaultSmsSubId() == INVALID_SUB_ID) {
            return false;
        }
        if (getDefaultVoiceSubId() == INVALID_SUB_ID) {
            return false;
        }
        return true;
    }

    /**
     * If a default is set to subscription which is not active, this will reset that default back to
     * INVALID_SUB_ID.
     * @hide
     */
    public static void clearDefaultsForInactiveSubIds() {
        if (VDBG) logd("clearDefaultsForInactiveSubIds");
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearDefaultsForInactiveSubIds();
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /**
     * @return true if a valid subId else false
     * @hide
     */
    public static boolean isValidSubscriptionId(int subId) {
        return subId > INVALID_SUBSCRIPTION_ID ;
    }

    /**
     * @return true if a valid subId else false
     * @hide - to be unhidden
     */
    public static boolean isValidSubId(int subId) {
        return subId > INVALID_SUB_ID ;
    }

    /** @hide */
    public static boolean isValidSlotId(int slotId) {
        // We are testing INVALID_SLOT_ID and slotId >= 0 independently because we should
        // not assume that INVALID_SLOT_ID will always be a negative value.  Any negative
        // value is invalid.
        return slotId != INVALID_SLOT_ID && slotId >= 0 &&
                slotId < TelephonyManager.getDefault().getSimCount();
    }

    /** @hide */
    public static boolean isValidPhoneId(int phoneId) {
        // We are testing INVALID_PHONE_ID and phoneId >= 0 independently because we should
        // not assume that INVALID_PHONE_ID will always be a negative value.  Any negative
        // value is invalid.
        return phoneId != INVALID_PHONE_ID && phoneId >= 0 &&
                phoneId < TelephonyManager.getDefault().getPhoneCount();
    }

    /** @hide */
    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds != null && subIds.length > 0) {
            putPhoneIdAndSubIdExtra(intent, phoneId, subIds[0]);
        } else {
            logd("putPhoneIdAndSubIdExtra: no valid subs");
        }
    }

    /** @hide */
    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId, int subId) {
        if (VDBG) logd("putPhoneIdAndSubIdExtra: phoneId=" + phoneId + " subId=" + subId);
        intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
        intent.putExtra(PhoneConstants.PHONE_KEY, phoneId);
        //FIXME this is using phoneId and slotId interchangeably
        //Eventually, this should be removed as it is not the slot id
        intent.putExtra(PhoneConstants.SLOT_KEY, phoneId);
    }

    /**
     * @return the list of subId's that are active,
     *         is never null but the length maybe 0.
     * @hide
     */
    public static int[] getActiveSubscriptionIdList() {
        int[] subId = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getActiveSubIdList();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (subId == null) {
            subId = new int[0];
        }

        return subId;

    }

    /**
     * @return the list of subId's that are activated,
     *         is never null but the length maybe 0.
     * {@hide}
     */
    public static int[] getActiveSubIdList() {
        // all that MTK renaming
        return getActiveSubscriptionIdList();
    }

    private static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        logd("StackTrace - " + msg);
        StackTraceElement[] st = re.getStackTrace();
        for (StackTraceElement ste : st) {
            logd(ste.toString());
        }
    }
}

