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

package io.supertokens.storage.sql.test;

import io.supertokens.ProcessState;
import io.supertokens.emailpassword.EmailPassword;
import io.supertokens.pluginInterface.KeyValueInfo;
import io.supertokens.pluginInterface.STORAGE_TYPE;
import io.supertokens.pluginInterface.Storage;
import io.supertokens.pluginInterface.emailpassword.PasswordResetTokenInfo;
import io.supertokens.pluginInterface.emailpassword.UserInfo;
import io.supertokens.pluginInterface.emailpassword.sqlStorage.EmailPasswordSQLStorage;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.session.sqlStorage.SessionSQLStorage;
import io.supertokens.pluginInterface.sqlStorage.SQLStorage;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.domainobject.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.domainobject.emailpassword.PasswordResetTokensDO;
import io.supertokens.storage.sql.domainobject.emailpassword.PasswordResetTokensPK;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storageLayer.StorageLayer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.PrintStream;

import static org.junit.Assert.assertNotNull;

public class HibernateTest {

    @Rule
    public TestRule watchman = Utils.getOnFailure();

    @AfterClass
    public static void afterTesting() {
        Utils.afterTesting();
        System.setOut(System.out);
        Start.printSQL = false;
    }

    @Before
    public void beforeEach() {
        Utils.reset();
        System.setOut(System.out);
        Start.printSQL = false;
    }

    @Test
    public void shouldSelectFromDbOnce()
            throws InterruptedException, StorageQueryException, StorageTransactionLogicException {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        Storage storage = StorageLayer.getStorage(process.getProcess());

        printInterceptor.start = true;

        SQLStorage sqlStorage = (SQLStorage) storage;
        sqlStorage.startTransaction(con -> {
            sqlStorage.getKeyValue_Transaction(con, "Key");
            sqlStorage.getKeyValue_Transaction(con, "Key");
            sqlStorage.setKeyValue_Transaction(con, "Key", new KeyValueInfo("Value2"));
            sqlStorage.setKeyValue_Transaction(con, "Key", new KeyValueInfo("Value3"));
            sqlStorage.commitTransaction(con);
            return null;
        });

        // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);
        assert (sqlStorage.getKeyValue("Key").value.equals("Value3"));

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    @Test
    public void shouldSelectFromDbTwice()
            throws InterruptedException, StorageQueryException, StorageTransactionLogicException {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        Storage storage = StorageLayer.getStorage(process.getProcess());
        printInterceptor.start = true;

        storage.setKeyValue("Key", new KeyValueInfo("Value1"));

        SQLStorage sqlStorage = (SQLStorage) storage;
        sqlStorage.startTransaction(con -> {
            sqlStorage.getKeyValue_Transaction(con, "Key");
            sqlStorage.setKeyValue_Transaction(con, "Key", new KeyValueInfo("Value2"));
            sqlStorage.commitTransaction(con);
            return null;
        });

        // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 2);

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    @Test
    public void shouldSelectFromDbTwiceWithoutInitialRowBecauseQueryingTwoDifferentKeys()
            throws InterruptedException, StorageQueryException, StorageTransactionLogicException {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        Storage storage = StorageLayer.getStorage(process.getProcess());
        printInterceptor.start = true;

        SQLStorage sqlStorage = (SQLStorage) storage;
        sqlStorage.startTransaction(con -> {
            sqlStorage.getKeyValue_Transaction(con, "Key");
            sqlStorage.setKeyValue_Transaction(con, "Key", new KeyValueInfo("Value2"));
            sqlStorage.getKeyValue_Transaction(con, "Key1");
            sqlStorage.setKeyValue_Transaction(con, "Key1", new KeyValueInfo("Value2"));
            sqlStorage.setKeyValue_Transaction(con, "Key", new KeyValueInfo("Value3"));
            sqlStorage.setKeyValue_Transaction(con, "Key1", new KeyValueInfo("Value3"));
            sqlStorage.commitTransaction(con);
            return null;
        });

        // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 2);
        assert (sqlStorage.getKeyValue("Key1").value.equals("Value3"));
        assert (sqlStorage.getKeyValue("Key").value.equals("Value3"));

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    @Test
    public void shouldSelectFromDbOnceDuringSetAndDelete()
            throws InterruptedException, StorageQueryException, StorageTransactionLogicException {

        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        Storage storage = StorageLayer.getStorage(process.getProcess());

        printInterceptor.start = true;

        SessionSQLStorage sqlStorage = (SessionSQLStorage) storage;
        sqlStorage.startTransaction(con -> {
            sqlStorage.getKeyValue_Transaction(con, "access_token_signing_key");
            sqlStorage.getKeyValue_Transaction(con, "access_token_signing_key");
            sqlStorage.setKeyValue_Transaction(con, "access_token_signing_key", new KeyValueInfo("Value2"));
            sqlStorage.setKeyValue_Transaction(con, "access_token_signing_key", new KeyValueInfo("Value3"));
            sqlStorage.removeLegacyAccessTokenSigningKey_Transaction(con);
            sqlStorage.commitTransaction(con);
            return null;
        });

        // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);
        assert (sqlStorage.getKeyValue("access_token_signing_key") == null);

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));

    }

    @Test
    public void selectOnceFromDbDuringDelete()
            throws InterruptedException, StorageQueryException, StorageTransactionLogicException {
        {
            String[] args = { "../" };
            enableSQLLogging();
            Interceptor printInterceptor = new Interceptor();
            System.setOut(printInterceptor);
            TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

            Storage storage = StorageLayer.getStorage(process.getProcess());

            storage.setKeyValue("access_token_signing_key", new KeyValueInfo("Value"));

            printInterceptor.start = true;

            SessionSQLStorage sqlStorage = (SessionSQLStorage) storage;
            sqlStorage.startTransaction(con -> {
                sqlStorage.getKeyValue_Transaction(con, "access_token_signing_key");
                sqlStorage.removeLegacyAccessTokenSigningKey_Transaction(con);
                sqlStorage.commitTransaction(con);
                return null;
            });

            // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
            assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);
            assert (sqlStorage.getKeyValue("access_token_signing_key") == null);

            process.kill();
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
        }

        {
            String[] args = { "../" };
            enableSQLLogging();
            Interceptor printInterceptor = new Interceptor();
            System.setOut(printInterceptor);
            TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

            Storage storage = StorageLayer.getStorage(process.getProcess());

            printInterceptor.start = true;

            SessionSQLStorage sqlStorage = (SessionSQLStorage) storage;
            sqlStorage.startTransaction(con -> {
                sqlStorage.removeLegacyAccessTokenSigningKey_Transaction(con);
                sqlStorage.commitTransaction(con);
                return null;
            });

            // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
            assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);
            assert (sqlStorage.getKeyValue("access_token_signing_key") == null);

            process.kill();
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
        }

        {
            String[] args = { "../" };
            enableSQLLogging();
            Interceptor printInterceptor = new Interceptor();
            System.setOut(printInterceptor);
            TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

            Storage storage = StorageLayer.getStorage(process.getProcess());

            printInterceptor.start = true;

            SessionSQLStorage sqlStorage = (SessionSQLStorage) storage;
            sqlStorage.startTransaction(con -> {
                sqlStorage.setKeyValue_Transaction(con, "access_token_signing_key", new KeyValueInfo("Value"));
                sqlStorage.removeLegacyAccessTokenSigningKey_Transaction(con);
                sqlStorage.commitTransaction(con);
                return null;
            });

            // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
            assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);
            assert (sqlStorage.getKeyValue("access_token_signing_key") == null);

            process.kill();
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
        }

        {
            String[] args = { "../" };
            enableSQLLogging();
            Interceptor printInterceptor = new Interceptor();
            System.setOut(printInterceptor);
            TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

            Storage storage = StorageLayer.getStorage(process.getProcess());

            printInterceptor.start = true;

            SessionSQLStorage sqlStorage = (SessionSQLStorage) storage;
            sqlStorage.startTransaction(con -> {
                sqlStorage.getKeyValue_Transaction(con, "access_token_signing_key");
                sqlStorage.setKeyValue_Transaction(con, "access_token_signing_key", new KeyValueInfo("Value"));
                sqlStorage.removeLegacyAccessTokenSigningKey_Transaction(con);
                sqlStorage.commitTransaction(con);
                return null;
            });

            // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
            assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);
            assert (sqlStorage.getKeyValue("access_token_signing_key") == null);

            process.kill();
            assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
        }
    }

    @Test
    public void selectThenDeleteThenSet()
            throws InterruptedException, StorageQueryException, StorageTransactionLogicException {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        Storage storage = StorageLayer.getStorage(process.getProcess());

        printInterceptor.start = true;

        SessionSQLStorage sqlStorage = (SessionSQLStorage) storage;
        sqlStorage.startTransaction(con -> {
            sqlStorage.getKeyValue_Transaction(con, "access_token_signing_key");
            sqlStorage.getKeyValue_Transaction(con, "access_token_signing_key");
            sqlStorage.setKeyValue_Transaction(con, "access_token_signing_key", new KeyValueInfo("Value2"));
            sqlStorage.removeLegacyAccessTokenSigningKey_Transaction(con);
            sqlStorage.setKeyValue_Transaction(con, "access_token_signing_key", new KeyValueInfo("Value2"));
            sqlStorage.commitTransaction(con);
            return null;
        });

        // We do -1 cause if there is one occurrence of this, it will split the string into 2 parts
        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);
        assert (sqlStorage.getKeyValue("access_token_signing_key").value.equals("Value2"));

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    @Test
    public void testThatUsingCreateQuerySavesItemInTheCacheCorrectly() throws Exception {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        if (StorageLayer.getStorage(process.getProcess()).getType() != STORAGE_TYPE.SQL) {
            return;
        }

        UserInfo user = EmailPassword.signUp(process.getProcess(), "random@gmail.com", "validPass123");

        EmailPassword.generatePasswordResetToken(process.getProcess(), user.id);

        EmailPasswordSQLStorage storage = StorageLayer.getEmailPasswordStorage(process.getProcess());

        printInterceptor.start = true;
        storage.startTransaction(con -> {
            PasswordResetTokenInfo[] result = storage.getAllPasswordResetTokenInfoForUser_Transaction(con, user.id);
            PasswordResetTokensDO p = new PasswordResetTokensDO();
            PasswordResetTokensPK pk = new PasswordResetTokensPK();
            pk.setToken(result[0].token);
            EmailPasswordUsersDO epUser = new EmailPasswordUsersDO();
            epUser.setUser_id(user.id);
            pk.setUser(epUser);
            p.setPk(pk);
            CustomSessionWrapper session = (CustomSessionWrapper) con.getSession();
            PasswordResetTokensDO other = session.get(PasswordResetTokensDO.class, pk);
            if (!other.getPk().getToken().equals(result[0].token) || !other.getPk().getUserId().equals(user.id)) {
                throw new StorageTransactionLogicException(new Exception("Test failed!"));
            }
            return null;
        });
        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 1);

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    @Test
    public void temp() throws Exception {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        // System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        if (StorageLayer.getStorage(process.getProcess()).getType() != STORAGE_TYPE.SQL) {
            return;
        }

        UserInfo user = EmailPassword.signUp(process.getProcess(), "random@gmail.com", "validPass123");

        EmailPassword.generatePasswordResetToken(process.getProcess(), user.id);

        EmailPasswordSQLStorage storage = StorageLayer.getEmailPasswordStorage(process.getProcess());

        printInterceptor.start = true;
        storage.startTransaction(con -> {
            PasswordResetTokenInfo[] result = storage.getAllPasswordResetTokenInfoForUser_Transaction(con, user.id);
            PasswordResetTokensDO p = new PasswordResetTokensDO();
            PasswordResetTokensPK pk = new PasswordResetTokensPK();
            pk.setToken(result[0].token);
            EmailPasswordUsersDO epUser = new EmailPasswordUsersDO();
            epUser.setUser_id(user.id);
            pk.setUser(epUser);
            p.setPk(pk);
            CustomSessionWrapper session = (CustomSessionWrapper) con.getSession();
            PasswordResetTokensDO other = session.get(PasswordResetTokensDO.class, pk);
            p.getPk().equals(other.getPk());
            return null;
        });

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    @Test
    public void testThatUsingSessionSaveOnPartialPkDoesOnlyInsert() throws Exception {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        if (StorageLayer.getStorage(process.getProcess()).getType() != STORAGE_TYPE.SQL) {
            return;
        }

        UserInfo user = EmailPassword.signUp(process.getProcess(), "random@gmail.com", "validPass123");

        printInterceptor.start = true;
        EmailPassword.generatePasswordResetToken(process.getProcess(), user.id);

        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 0);
        assert (printInterceptor.s.split("Hibernate: insert").length - 1 == 1);

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    @Test
    public void testThatDeletingAUserDoesNotUseSelect() throws Exception {
        String[] args = { "../" };
        enableSQLLogging();
        Interceptor printInterceptor = new Interceptor();
        System.setOut(printInterceptor);
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        if (StorageLayer.getStorage(process.getProcess()).getType() != STORAGE_TYPE.SQL) {
            return;
        }

        UserInfo user = EmailPassword.signUp(process.getProcess(), "random@gmail.com", "validPass123");

        printInterceptor.start = true;
        StorageLayer.getEmailPasswordStorage(process.getProcess()).deleteEmailPasswordUser(user.id);

        assert (printInterceptor.s.split("Hibernate: select").length - 1 == 0);
        assert (printInterceptor.s.split("Hibernate: delete").length - 1 == 2); // one delete from emailpassword users
        // table, and the other from all auth
        // users table

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    private static void enableSQLLogging() {
        Start.printSQL = true;
        StorageLayer.close();
    }

    private static class Interceptor extends PrintStream {
        public String s = "";
        public boolean start = false;

        public Interceptor() {
            super(System.out, true);
        }

        @Override
        public void print(String s) {// do what ever you like
            if (s == null) {
                return;
            }
            if (start) {
                this.s = this.s + s;
            }
        }
    }
}
