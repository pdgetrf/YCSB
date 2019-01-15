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
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.options.GetOption;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Charsets.UTF_8;


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
      Map<CompletableFuture<GetResponse>, String> responseMap = new HashMap<>();

      if (fields==null) {
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
        for (String field : fields) {
          String path = "/" + key + "/" + field;
          CompletableFuture<GetResponse> futureResponse = client.getKVClient().get(
              ByteSequence.fromString(path),
              GetOption.newBuilder().withRevision(0).build()
          );
          responseMap.put(futureResponse, field);
        }
      }

      // wait for all requests
      responseMap.keySet().forEach(CompletableFuture::join);

      // create the result
      for (Map.Entry<CompletableFuture<GetResponse>, String> entry : responseMap.entrySet()) {

        List<KeyValue> kvs = entry.getKey().get().getKvs();
        if (kvs == null || kvs.isEmpty()) {
          return Status.NOT_FOUND;
        }

        String val = kvs.get(0).getValue().toString(UTF_8);
        result.put(entry.getValue(), new StringByteIterator(val));
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
      Map<CompletableFuture<PutResponse>, String> responseMap = new HashMap<>();
      for (String keyToInsert : values.keySet()) {
        String path = "/" + key + "/" + keyToInsert;

        CompletableFuture<PutResponse> futureResponse = client.getKVClient().put(
            ByteSequence.fromString(path),
            ByteSequence.fromString(values.get(keyToInsert).toString())
        );
        responseMap.put(futureResponse, path);
      }

      // wait for all requests
      responseMap.keySet().forEach(CompletableFuture::join);
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

    String path = "/" + key + "/";
    ByteSequence keySeq = ByteSequence.fromString(path);

    GetOption option = GetOption.newBuilder()
        .withPrefix(keySeq)
        .build();

    try {
      GetResponse response = client.getKVClient().get(keySeq, option).get();
      if (response.getKvs().isEmpty()) {
        log.info("Failed to retrieve any key.");
        return null;
      }

      for (KeyValue kv : response.getKvs()) {
        client.getKVClient().delete(kv.getKey()).get();
      }
      return Status.OK;
    } catch (Exception e) {
      log.error(String.format("Error deleting key: %s ", key), e);
      return Status.ERROR;
    }
  }
}
