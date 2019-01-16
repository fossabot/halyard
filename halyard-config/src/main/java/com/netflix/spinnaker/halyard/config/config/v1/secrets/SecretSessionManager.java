package com.netflix.spinnaker.halyard.config.config.v1.secrets;

import com.netflix.spinnaker.kork.secrets.EncryptedSecret;
import com.netflix.spinnaker.kork.secrets.SecretManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *  SecretSessionManager handles the decryption and encryption of secrets and secret files in the current session
 */
@Component
public class SecretSessionManager {
  private static ThreadLocal<SecretSession> secretSessions = new ThreadLocal<>();

  @Autowired
  private SecretManager secretManager;

  public void clearSession() {
    SecretSession session = secretSessions.get();
    if (session != null) {
      session.clearAllFiles();
      secretSessions.remove();
    }
  }

  public SecretSession getSession() {
    SecretSession session = secretSessions.get();
    if (session == null) {
      session = new SecretSession();
      secretSessions.set(session);
    }
    return session;
  }

  public String decrypt(String encryptedString) {
    if (!EncryptedSecret.isEncryptedSecret(encryptedString)) {
      return encryptedString;
    }

    SecretSession session = getSession();
    String clearText = session.getCached(encryptedString);
    if (clearText != null) {
      return clearText;
    }

    clearText = secretManager.decrypt(encryptedString);
    session.cacheResult(encryptedString, clearText);

    return clearText;
  }

  public String decryptFile(String filePathOrEncrypted) {
    if (!EncryptedSecret.isEncryptedSecret(filePathOrEncrypted)) {
      return filePathOrEncrypted;
    }

    SecretSession session = getSession();
    String tempFilePath = session.getCached(filePathOrEncrypted);
    if (tempFilePath != null){
      return tempFilePath;
    }

    tempFilePath = secretManager.decryptFile(filePathOrEncrypted);
    session.addFile(tempFilePath);
    session.cacheResult(filePathOrEncrypted, tempFilePath);

    return tempFilePath;
  }

  public String encrypt(String unencryptedString) {
    if (EncryptedSecret.isEncryptedSecret(unencryptedString)) {
      return unencryptedString;
    }
    // Encryption not currently supported
    return unencryptedString;
  }

}