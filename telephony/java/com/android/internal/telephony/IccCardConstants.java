/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.internal.telephony;

/**
 * {@hide}
 */
public class IccCardConstants {

    /* The extra data for broacasting intent INTENT_ICC_STATE_CHANGE */
    public static final String INTENT_KEY_ICC_STATE = "ss";
    /* UNKNOWN means the ICC state is unknown */
    public static final String INTENT_VALUE_ICC_UNKNOWN = "UNKNOWN";
    /* NOT_READY means the ICC interface is not ready (eg, radio is off or powering on) */
    public static final String INTENT_VALUE_ICC_NOT_READY = "NOT_READY";
    /* ABSENT means ICC is missing */
    public static final String INTENT_VALUE_ICC_ABSENT = "ABSENT";
    /* CARD_IO_ERROR means for three consecutive times there was SIM IO error */
    static public final String INTENT_VALUE_ICC_CARD_IO_ERROR = "CARD_IO_ERROR";
    /* LOCKED means ICC is locked by pin or by network */
    public static final String INTENT_VALUE_ICC_LOCKED = "LOCKED";
    /* READY means ICC is ready to access */
    public static final String INTENT_VALUE_ICC_READY = "READY";
    /* IMSI means ICC IMSI is ready in property */
    public static final String INTENT_VALUE_ICC_IMSI = "IMSI";
    /* LOADED means all ICC records, including IMSI, are loaded */
    public static final String INTENT_VALUE_ICC_LOADED = "LOADED";
    /* The extra data for broacasting intent INTENT_ICC_STATE_CHANGE */
    public static final String INTENT_KEY_LOCKED_REASON = "reason";
    /* PIN means ICC is locked on PIN1 */
    public static final String INTENT_VALUE_LOCKED_ON_PIN = "PIN";
    /* PUK means ICC is locked on PUK1 */
    public static final String INTENT_VALUE_LOCKED_ON_PUK = "PUK";
    /* NETWORK means ICC is locked on NETWORK PERSONALIZATION */
    public static final String INTENT_VALUE_LOCKED_NETWORK = "NETWORK";
    /* PERSO means ICC is locked on PERSONALIZATION */
    public static final String INTENT_VALUE_LOCKED_PERSO = "PERSO";
    /* PERM_DISABLED means ICC is permanently disabled due to puk fails */
    public static final String INTENT_VALUE_ABSENT_ON_PERM_DISABLED = "PERM_DISABLED";

    // Added by M begin
    /* NETWORK_SUBSET means ICC is locked on NETWORK SUBSET PERSONALIZATION */
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET = "NETWORK_SUBSET";
    /* CORPORATE means ICC is locked on CORPORATE PERSONALIZATION */
    public static final String INTENT_VALUE_LOCKED_CORPORATE = "CORPORATE";
    /* SERVICE_PROVIDER means ICC is locked on SERVICE_PROVIDER PERSONALIZATION */
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER = "SERVICE_PROVIDER";
    /* SIM means ICC is locked on SIM PERSONALIZATION */
    public static final String INTENT_VALUE_LOCKED_SIM = "SIM";
    // Added by M end

    /**
     * This is combination of IccCardStatus.CardState and IccCardApplicationStatus.AppState
     * for external apps (like PhoneApp) to use
     *
     * UNKNOWN is a transient state, for example, after user inputs ICC pin under
     * PIN_REQUIRED state, the query for ICC status returns UNKNOWN before it
     * turns to READY
     */
    public enum State {
        UNKNOWN,
        ABSENT,
        PIN_REQUIRED,
        PUK_REQUIRED,
        PERSO_LOCKED, /** ordinal(4) == {@See TelephonyManager#SIM_STATE_NETWORK_LOCKED} */
        READY,
        NOT_READY,
        PERM_DISABLED,
        CARD_IO_ERROR,
        NETWORK_LOCKED;

        public boolean isPinLocked() {
            return ((this == PIN_REQUIRED) || (this == PUK_REQUIRED));
        }

        public boolean iccCardExist() {
            return ((this == PIN_REQUIRED) || (this == PUK_REQUIRED)
                    || (this == NETWORK_LOCKED) || (this == READY)
                    || (this == PERM_DISABLED) || (this == CARD_IO_ERROR));
        }

        public static State intToState(int state) throws IllegalArgumentException {
            switch(state) {
                case 0: return UNKNOWN;
                case 1: return ABSENT;
                case 2: return PIN_REQUIRED;
                case 3: return PUK_REQUIRED;
                case 4: return PERSO_LOCKED;
                case 5: return READY;
                case 6: return NOT_READY;
                case 7: return PERM_DISABLED;
                case 8: return CARD_IO_ERROR;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
