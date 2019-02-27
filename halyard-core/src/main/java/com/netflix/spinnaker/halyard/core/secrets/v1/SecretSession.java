/*
 * Copyright 2019 Armory, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.halyard.core.secrets.v1;

import com.netflix.spinnaker.config.secrets.SecretManager;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  SecretSession contains the cached decrypted secrets and secret files
 */
class SecretSession {

  private SecretManager secretManager;

  public SecretSession(SecretManager secretManager) {
    this.secretManager = secretManager;
  }

  private Map<String, Path> tempFiles = new HashMap<>();

  void addFile(String encrypted, Path decryptedFilePath) {
    tempFiles.put(encrypted, decryptedFilePath);
  }

  void clearCache() {
    secretManager.clearCachedSecrets();
  }

  void clearTempFiles() {
    Set<String> filePaths = tempFiles.keySet();
    for (String fp : filePaths) {
      secretManager.clearCachedFile(fp);
      File f = new File(tempFiles.get(fp).toString());
      if (f.delete()) {
        tempFiles.remove(fp);
      }
    }
  }
}
