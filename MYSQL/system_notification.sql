
--
-- Table structure for table `system_notification`
--

DROP TABLE IF EXISTS `system_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `admin_id` bigint NOT NULL COMMENT '发布管理员ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_notification`
--

LOCK TABLES `system_notification` WRITE;
/*!40000 ALTER TABLE `system_notification` DISABLE KEYS */;
INSERT INTO `system_notification` VALUES (1,'fdsf','dfsdfsdf',7,'2026-05-12 09:17:54'),(2,'hgfhx','gfdgdfg',7,'2026-05-12 12:01:54'),(3,'fdsfd','sdfafasdf',7,'2026-05-12 12:46:15'),(4,'1111111111111111111','11111111111111111111111111111111111',7,'2026-05-12 14:09:20'),(5,'eee','gfddddddddddddddddddddddddd',7,'2026-05-12 19:18:58'),(6,'rerwe','dfffffffffffffffffffffffffa',7,'2026-05-13 07:50:42'),(7,'fgdfg','dfgdfgdfgdfggd\nhfghf',7,'2026-05-13 08:15:01'),(8,'erwe','fsdfsdf',7,'2026-05-13 08:15:55'),(9,'gg','gfdgdfg',7,'2026-05-13 08:16:52'),(10,'11111111','11111111111',7,'2026-05-13 14:02:09');
/*!40000 ALTER TABLE `system_notification` ENABLE KEYS */;
UNLOCK TABLES;
