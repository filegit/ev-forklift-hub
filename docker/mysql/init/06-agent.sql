-- Agent 服务专用库：对话持久化、执行日志、向量索引
CREATE DATABASE IF NOT EXISTS efh_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE efh_agent;

CREATE TABLE IF NOT EXISTS agent_conversation (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    session_id      VARCHAR(64)  NOT NULL COMMENT '会话ID，多实例共享',
    title           VARCHAR(200) DEFAULT NULL,
    summary         TEXT         DEFAULT NULL COMMENT '长期记忆摘要',
    message_count   INT          DEFAULT 0,
    total_tokens    INT          DEFAULT 0,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_session (user_id, session_id)
) COMMENT 'Agent 会话（长期记忆载体）';

CREATE TABLE IF NOT EXISTS agent_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT       NOT NULL,
    role            VARCHAR(20)  NOT NULL COMMENT 'user/assistant/system/tool',
    content         TEXT         NOT NULL,
    token_count     INT          DEFAULT 0,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv (conversation_id)
) COMMENT 'Agent 历史消息';

CREATE TABLE IF NOT EXISTS agent_execution_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    trace_id        VARCHAR(64)  NOT NULL COMMENT '单次请求链路ID',
    user_id         BIGINT       DEFAULT NULL,
    session_id      VARCHAR(64)  DEFAULT NULL,
    step_order      INT          DEFAULT 0,
    agent_name      VARCHAR(50)  DEFAULT NULL,
    tool_name       VARCHAR(50)  DEFAULT NULL,
    step_type       VARCHAR(30)  NOT NULL COMMENT 'SAFETY/RAG/TOOL/LLM/PLAN/MEMORY',
    input_text      TEXT,
    output_text     TEXT,
    duration_ms     BIGINT       DEFAULT 0,
    prompt_tokens   INT          DEFAULT 0,
    completion_tokens INT        DEFAULT 0,
    cost_yuan       DECIMAL(10,6) DEFAULT 0,
    success         TINYINT      DEFAULT 1,
    error_msg       VARCHAR(500) DEFAULT NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_trace (trace_id),
    INDEX idx_user_session (user_id, session_id),
    INDEX idx_create_time (create_time)
) COMMENT 'Agent 执行链路日志';

CREATE TABLE IF NOT EXISTS agent_vector_index (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_type     VARCHAR(20)  NOT NULL COMMENT 'knowledge/post/comment',
    source_id       BIGINT       NOT NULL,
    chunk_index     INT          DEFAULT 0,
    title           VARCHAR(200) DEFAULT NULL,
    content         TEXT         NOT NULL,
    embedding_json  TEXT         COMMENT '向量JSON数组',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_source_chunk (source_type, source_id, chunk_index)
) COMMENT '向量索引（向量数据库持久层）';
