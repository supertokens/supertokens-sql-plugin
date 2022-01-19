/*
 *    Copyright (c) 2022, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */

package io.supertokens.storage.sql;

import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.math.BigInteger;
import java.util.List;

public class TestConstants {
    public static final String EMAIL = "sample@email.com";
    public static final String PASS_HASH = "4FC83A05D38771371FC53AA81AAD869C1FEB7A28";
    public static final long CREATED_AT = 10l;
    public static final String USER_ID = "sample_user";
    public static final String TOKEN = "sample_token";
    public static final long TOKEN_EXPIRY = 20l;

    public static final String KEY = "key";
    public static final String KEY_STRING = "key_string";
    public static final String ALGORITHM = "algorithm";

    public static final String SESSION_HANDLE = "SESSION_HANDLE";
    public static final String REFRESH_TOKEN_HASH_TWO = "REFRESH_TOKEN_HASH_TWO";
    public static final String SESSION_DATA = "SESSION_DATA";
    public static final long EXPIRES_AT = 50l;
    public static final String JWT_USER_PAYLOAD = "JWT_USER_PAYLOAD";
    public static final String VALUE = "VALUE";

    public static final String THIRD_PARTY_ID = "THIRD_PARTY_ID";
    public static final String THIRD_PARTY_USER_ID = "THIRD_PARTY_USER_ID";
    public static final long TIME_JOINED = 20l;

    public static final String DEVICE_ID_HASH = "DEVICE_ID_HASH";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String LINK_CODE_SALT = "LINK_CODE_SALT";
    public static final int FAILED_ATTEMPTS = 2;

}
