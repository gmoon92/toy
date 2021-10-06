package com.gmoon.hibernateenvers.global.utils;

import com.gmoon.hibernateenvers.global.annotation.TODO;
import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;
import com.gmoon.hibernateenvers.global.vo.BaseRevisionCompareVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@Slf4j
public class RevisionConverter {

  /**
   * Serializable byte array -> DataBase save
   * https://www.javaspecialists.eu/archive/Issue020.html
   * https://stackoverflow.com/questions/26901706/how-to-save-a-serialized-object-in-a-database
   *
   * @author moong
   */
  @TODO
  public static byte[] serializedObject(Serializable entityId) {
    byte[] bytes = null;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
      out.writeObject(entityId);
      out.flush();
      bytes = bos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Entity ID type serialization is not converted to a byte array", e);
    } finally {
      log.info("serializedObject ({}) : {}", bytes.length, bytes);
      return bytes;
    }
  }

  /**
   * 저장된 byte array에 해당된 Object의
   * 필드명이 바뀌거나, 새로운 필드값에 대해선 추적 불가능.
   * https://www.baeldung.com/junit-assert-exception
   *
   * @author moong
   */
  public static Object deSerializedObject(byte[] entityId) {
    Assert.notNull(entityId, "Error with entityId parameter null");

    Object deSerializedObject = null;
    try (ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(entityId))) {
      deSerializedObject = objectIn.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Entity ID type byte array is not converted to object", e);
    } finally {
      log.info("deSerializedObject : {}", deSerializedObject);
      return deSerializedObject;
    }
  }

  public static <Entity extends BaseTrackingEntity, Convert extends BaseRevisionCompareVO> Convert convertTo(Entity entity, Class<Convert> convertClass) {
    Assert.notNull(entity, "Error with entity parameter null");

    try {
      Convert convert = convertClass.newInstance();
      BeanUtils.copyProperties(entity, convert);
      return convert;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Does Not convert to EntityCompareVO object", e);
    }
  }
}