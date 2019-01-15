package com.netflix.spinnaker.halyard.config.config.v1.secrets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *  SecretSession contains the cached decrypted secrets and secret files
 */

class SecretSession {
  private Map<String, String> cache = new HashMap<>();
  private List<String> filePaths = new ArrayList<>();


  void cacheResult(String encryptedString, String clearText) {
    cache.put(encryptedString, clearText);
  }

  void addFile(String filePath) {
    filePaths.add(filePath);
  }

  String getCached(String encryptedString) {
    return cache.get(encryptedString);
  }

  void clearAllFiles() {
    for (String filePath : filePaths) {
      File f = new File(filePath);
      f.delete();
    }
  }


}
