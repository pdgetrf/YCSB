/**
 * Copyright (c) 2013-2018 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. See accompanying LICENSE file.
 *
 */
package com.yahoo.ycsb.db.etcd;

import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Etcd client.
 * <p>
 * See {@code etcd/README.md} for details.
 */
public class EtcdClient extends EtcdAbstractClient {
  /**
   *
   */
  private static Logger log = LogManager.getLogger(EtcdClient.class);

  private final ConcurrentHashMap<String, HashMap<String, StringByteIterator>> localCache = new ConcurrentHashMap<>();

  /**
   * Read a record from the database. Each field/value pair from the result will
   * be stored in a HashMap.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to read.
   * @param fields The list of fields to read, or null for all of them
   * @param result A HashMap of field/value pairs for the result
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status read(String table, String key, Set<String> fields,
                     Map<String, ByteIterator> result) {

    try {
      if (fields == null) {
        String path = "/" + key + "/";
        ByteSequence keySeq = ByteSequence.fromString(path);
        GetOption option = GetOption.newBuilder()
            .withPrefix(keySeq)
            .build();

        GetResponse response = client.getKVClient().get(keySeq, option).get();
        if (response.getKvs().isEmpty()) {
          log.info("Failed to retrieve any key.");
          return null;
        }
      } else {
        String value = null;
        HashMap<String, StringByteIterator> hotFields = null;

        // get value from local cache or etcd
        if (localCache.containsKey(key)) {
          hotFields = localCache.get(key);
        } else {
          GetResponse response = client.getKVClient().get(
              ByteSequence.fromString(key),
              GetOption.newBuilder().withRevision(0).build()
          ).get();

          List<KeyValue> kvs = response.getKvs();
          if (kvs==null || kvs.isEmpty()) {
            return Status.NOT_FOUND;
          }
          value = kvs.get(0).getValue().toStringUtf8();

          Map<String, String> map = (HashMap<String, String>) deserialize(value);
          hotFields = new HashMap<>();
          for (Map.Entry<String, String> entry : map.entrySet()) {
            hotFields.put(entry.getKey(), new StringByteIterator(entry.getValue()));
          }
          localCache.put(key, hotFields);
        }

        // find fields from value map
        boolean found = false;
        for (String field: fields) {
          if (hotFields.containsKey(field)) {
            result.put(field, hotFields.get(field));
            found = true;
          }
        }
        if (!found) {
          return Status.NOT_FOUND;
        }
      }
    } catch (Exception e) {
      log.error(String.format("Error reading key: %s", key), e);
      return Status.ERROR;
    }

    return Status.OK;
  }

  /**
   * Update a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key, overwriting any existing values with the same field name.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to write.
   * @param values A HashMap of field/value pairs to update in the record
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status update(String table, String key,
                       Map<String, ByteIterator> values) {

    return insert(table, key, values);
  }

  private static String serialize(Serializable o) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(o);
    oos.close();
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  private static Object deserialize(String s) throws IOException,
      ClassNotFoundException {
    byte[] data = Base64.getDecoder().decode(s);
    ObjectInputStream ois = new ObjectInputStream(
        new ByteArrayInputStream(data));
    Object o = ois.readObject();
    ois.close();
    return o;
  }

  /**
   * Insert a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to insert.
   * @param values A HashMap of field/value pairs to insert in the record
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status insert(String table, String key,
                       Map<String, ByteIterator> values) {

    if (values == null || values.isEmpty()) {
      return Status.ERROR;
    }

    try {
      Map<String, String> strMap = new HashMap<>();
      for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
        strMap.put(entry.getKey(), entry.getValue().toString());
      }

      String value = serialize((Serializable) strMap);

      // invalidated cache if needed
      if (localCache.containsKey(key)) {
        localCache.remove(key);
      }

      client.getKVClient().put(
          ByteSequence.fromString(key),
          ByteSequence.fromString(value)
      ).get();
    } catch (Exception e) {
      log.error(String.format("Error inserting key: %s", key), e);
      return Status.ERROR;
    }
    return Status.OK;
  }

  /**
   * Delete a record from the database.
   *
   * @param table The name of the table
   * @param key   The record key of the record to delete.
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status delete(String table, String key) {
    try {
      client.getKVClient().delete(ByteSequence.fromString(key)).get();
      return Status.OK;
    } catch (Exception e) {
      log.error(String.format("Error deleting key: %s ", key), e);
      return Status.ERROR;
    }
  }
}
