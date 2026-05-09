package com.yishuifengxiao.common.tool.jdbc;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mocker_data")
public class MockerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint(20) NOT NULL AUTO_INCREMENT")
    private Long id;

    @Column(name = "int_data", columnDefinition = "int(11) DEFAULT NULL")
    private int intData;

    @Column(name = "integer_data", columnDefinition = "int(11) DEFAULT NULL")
    private Integer integerData;

    @Column(name = "tiny_int", columnDefinition = "tinyint(6) DEFAULT NULL")
    private Byte tinyInt;

    @Column(name = "small_int", columnDefinition = "smallint(6) DEFAULT NULL")
    private Short smallInt;

    @Column(name = "mid_int", columnDefinition = "mediumint(9) DEFAULT NULL")
    private Short midInt;

    @Column(name = "bigint", columnDefinition = "bigint(20) DEFAULT NULL")
    private long bigint;

    @Column(name = "decimal", columnDefinition = "decimal(10,2) DEFAULT NULL")
    private BigDecimal decimal;

    @Column(name = "char_text", columnDefinition = "char(10) DEFAULT NULL")
    private String charText;

    @Column(name = "varchar", columnDefinition = "varchar(205) DEFAULT NULL")
    private String varchar;

    @Column(name = "date", columnDefinition = "date DEFAULT NULL")
    private Date date;

    @Column(name = "datetime", columnDefinition = "datetime DEFAULT NULL")
    private LocalDateTime datetime;

    @Column(name = "time", columnDefinition = "time DEFAULT NULL")
    private LocalTime time;

    @Column(name = "timestamp", columnDefinition = "timestamp NULL DEFAULT NULL")
    private LocalDateTime timestamp;

    @Column(name = "local_date_time", columnDefinition = "datetime DEFAULT NULL")
    private LocalDateTime localDateTime;

    @Column(name = "instant", columnDefinition = "datetime DEFAULT NULL")
    private Instant instant;

    @Column(name = "zoned_date_time", columnDefinition = "datetime DEFAULT NULL")
    private ZonedDateTime zonedDateTime;

    @Column(name = "text", columnDefinition = "text")
    private String text;

    @Column(name = "mediumtext", columnDefinition = "mediumtext")
    private String mediumtext;

    @Column(name = "longtext", columnDefinition = "longtext")
    private String longtext;

    @Column(name = "tinytext", columnDefinition = "tinytext")
    private String tinytext;

    @Column(name = "blob", columnDefinition = "blob")
    private byte[] blob;

    @Column(name = "longblob", columnDefinition = "longblob")
    private byte[] longblob;

    @Column(name = "float", columnDefinition = "float DEFAULT NULL")
    private Float floatValue;

    @Column(name = "varbinary", columnDefinition = "varbinary(200) DEFAULT NULL")
    private byte[] varbinary;

    @Column(name = "json", columnDefinition = "json DEFAULT NULL")
    private String json;
}