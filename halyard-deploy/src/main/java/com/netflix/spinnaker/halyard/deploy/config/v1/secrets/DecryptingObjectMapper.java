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

package com.netflix.spinnaker.halyard.deploy.config.v1.secrets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.netflix.spinnaker.halyard.config.model.v1.node.Secret;
import com.netflix.spinnaker.halyard.config.model.v1.node.SecretFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public class DecryptingObjectMapper extends ObjectMapper {
    private enum SecretType {
        SECRET_FILE, SECRET, NO_SECRET
    }

    private List<String> files;

//    @Autowired
//    protected SecretSessionManager secretSessionManager;

    public DecryptingObjectMapper(List<String> files) {
        super();
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.files = files;

        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {

            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                Class _class = beanDesc.getBeanClass();

                for (Iterator<BeanPropertyWriter> it = beanProperties.iterator(); it.hasNext(); ) {
                    BeanPropertyWriter bpw = it.next();

                    switch(getFieldSecretType(_class, bpw.getName())) {
                        case SECRET:
                            // Decrypt the field secret before sending
                            bpw.assignSerializer(new StdScalarSerializer<Object>(String.class, false) {
                                @Override
                                public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                                    if (value != null) {
                                        String sValue = value.toString();
//                                        if (!EncryptedSecret.isEncryptedSecret(sValue)) {
//                                            gen.writeString(sValue);
//                                        }
                                        String decrypted = "decrypted";
                                        gen.writeString(decrypted);
                                    }
                                }
                            });
                            break;
                        case SECRET_FILE:
                            bpw.assignSerializer(new StdScalarSerializer<Object>(String.class, false) {
                                @Override
                                public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                                    if (value != null) {
                                        String sValue = value.toString();
                                        if (sValue.startsWith("encrypted")) {
                                            String decryptedPath = "newpath";
                                            for (int i = 0; i < files.size(); i++) {
                                                if (files.get(i).equals(sValue)) {
                                                    // Replace
                                                    files.set(i, decryptedPath);
                                                    break;
                                                }
                                            }
                                            gen.writeString(decryptedPath);
                                        } else {
                                            gen.writeString(sValue);
                                        }
                                    }
                                }
                            });
                    }
                }
                return beanProperties;
            }
        });
        this.registerModule(module);
    }

    protected SecretType getFieldSecretType(Class _class, String fieldName) {
        for (Field f : _class.getDeclaredFields()) {
            if (f.getName().equals(fieldName)) {
                if (f.isAnnotationPresent(Secret.class)) {
                    return SecretType.SECRET;
                }
                if (f.isAnnotationPresent(SecretFile.class)) {
                    return SecretType.SECRET_FILE;
                }
                return SecretType.NO_SECRET;
            }
        }
        if (_class.getSuperclass() != null) {
            return getFieldSecretType(_class.getSuperclass(), fieldName);
        }
        return SecretType.NO_SECRET;
    }
}
