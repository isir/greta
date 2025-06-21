/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.environmentmanager.core.io.message;

import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import java.util.Map;

/**
 *
 * @author Brice Donval
 */
public interface IMessageReceiver {

    public IEnvironmentServer getEnvironmentServer();

    /* ---------------------------------------------------------------------- */

    public String getHost();

    public void setHost(String host);

    /* ------------------------------ */

    public String getPort();

    public void setPort(String port);

    /* -------------------------------------------------- */

    public void hasConnected();

    public void hasDisconnected();

    /* -------------------------------------------------- */

    public void hasReceived(String message, Map<String, String> details);

    /* -------------------------------------------------- */

    public void destroy();

    public void onDestroy();

}
