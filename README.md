![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

> **Intelligent Link Monitoring System with Real-time Notifications**

Проект разработан в рамках курса "Академия Бэкенда" - современное микросервисное приложение для отслеживания обновлений контента по ссылкам с автоматическими уведомлениями в Telegram.

## 🏗️ Архитектура системы

Система построена на микросервисной архитектуре и состоит из следующих компонентов:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Telegram Bot  │◄──►│    Scrapper     │◄──►│   PostgreSQL    │
│    (Port 8080)  │    │   (Port 8081)   │    │   (Port 5433)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │              ┌─────────────────┐              │
         └──────────────►│     Kafka       │◄─────────────┘
                        │  (Port 29092)   │
                        └─────────────────┘
                                 │
                        ┌─────────────────┐
                        │   Monitoring    │
                        │ Grafana + Prom  │
                        └─────────────────┘
```

### Основные модули:

#### 🤖 **Bot Module**
- **Назначение**: Telegram-бот для взаимодействия с пользователями
- **Технологии**: Spring Boot 3.4.2, Java Telegram Bot API 7.11.0
- **Функциональность**:
    - Регистрация и управление пользователями
    - Добавление/удаление ссылок для отслеживания
    - Получение уведомлений об обновлениях
    - Rate limiting с Bucket4j
    - Retry механизмы с Resilience4j

#### 🔍 **Scrapper Module**
- **Назначение**: Сервис для мониторинга и анализа ссылок
- **Технологии**: Spring Boot 3.4.2, Spring Data JPA, Liquibase
- **Функциональность**:
    - Периодическое сканирование ссылок
    - Интеграция с GitHub API и Stack Overflow API
    - Обнаружение изменений контента
    - Отправка уведомлений через Kafka
    - REST API для взаимодействия с ботом

#### 📊 **Report Module**
- **Назначение**: Система аналитики и отчетности
- **Функциональность**: Генерация отчетов о работе системы

## 🛠️ Технологический стек

### Core Technologies
- **Java 23** - Современная версия с последними возможностями языка
- **Spring Boot 3.4.2** - Основной фреймворк
- **Spring WebFlux** - Реактивное программирование
- **Spring Security** - Безопасность приложения
- **Spring Data JPA** - Работа с базой данных

### Database & Messaging
- **PostgreSQL 17** - Основная база данных
- **Liquibase 4.29** - Управление миграциями БД
- **Apache Kafka** - Асинхронная обработка сообщений
- **Redis** - Кэширование (опционально)

### Monitoring & Observability
- **Prometheus** - Сбор метрик
- **Grafana** - Визуализация метрик
- **Micrometer** - Метрики приложения

### Development & Quality
- **Maven** - Сборка проекта
- **Docker & Docker Compose** - Контейнеризация
- **SpotBugs** - Статический анализ кода
- **PMD** - Анализ качества кода
- **JUnit 5** - Тестирование
- **WireMock** - Мокирование внешних API

## 🚀 Быстрый старт

### Предварительные требования
- **Java 23+**
- **Maven 3.8.8+**
- **Docker & Docker Compose**
- **Git**

### 1. Клонирование репозитория
```bash
git clone <repository-url>
cd java-Howepu
```

### 2. Настройка переменных окружения
Создайте файл `.env` в корне проекта:
```env
# Telegram Bot Configuration
TELEGRAM_TOKEN=your_telegram_bot_token

# GitHub Integration
GITHUB_TOKEN=your_github_token

# Stack Overflow Integration (optional)
SO_ACCESS_TOKEN=your_stackoverflow_token

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/scrapper
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis Configuration (optional)
SPRING_DATA_REDIS_HOST=redis

# Service URLs
SCRAPPER_BASE_URL=http://scrapper:8081
BOT_BASE_URL=http://bot:8080
```

### 3. Запуск инфраструктуры
```bash
# Запуск всех сервисов
docker-compose up -d

# Проверка статуса сервисов
docker-compose ps
```

### 4. Сборка и запуск приложений
```bash
# Сборка всех модулей
mvn clean install

# Запуск Scrapper
cd scrapper && mvn spring-boot:run

# Запуск Bot (в новом терминале)
cd bot && mvn spring-boot:run
```

## 📋 API Documentation

### Bot API Endpoints
- **Health Check**: `GET /actuator/health`
- **Metrics**: `GET /actuator/prometheus`
- **Updates**: `POST /updates` - Получение обновлений от Scrapper

### Scrapper API Endpoints
- **Health Check**: `GET /actuator/health`
- **Links Management**:
    - `GET /links` - Получить все отслеживаемые ссылки
    - `POST /links` - Добавить новую ссылку
    - `DELETE /links` - Удалить ссылку
- **Chat Management**:
    - `POST /tg-chat/{id}` - Регистрация чата
    - `DELETE /tg-chat/{id}` - Удаление чата

Полная документация API доступна через Swagger UI:
- Bot: `http://localhost:8080/swagger-ui.html`
- Scrapper: `http://localhost:8081/swagger-ui.html`

## 🔧 Конфигурация

### Основные настройки приложения

#### application.yml (Bot)
```yaml
app:
  telegram-token: ${TELEGRAM_TOKEN}
  scrapper-base-url: ${SCRAPPER_BASE_URL:http://localhost:8081}
  
bucket4j:
  enabled: true
  filters:
    - cache-name: rate-limit-buckets
      rate-limits:
        - bandwidths:
            - capacity: 100
              time: 1
              unit: minutes
```

#### application.yml (Scrapper)
```yaml
app:
  scheduler:
    enable: true
    interval: 10000
  database-access-type: jpa
  
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

## 🏃‍♂️ Развертывание

### Production Deployment

1. **Подготовка окружения**:
```bash
# Создание production конфигурации
cp .env.example .env.production
# Настройте production переменные
```

2. **Сборка Docker образов**:
```bash
# Сборка всех сервисов
docker-compose -f docker-compose.prod.yml build
```

3. **Запуск в production**:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Мониторинг в production
- **Grafana Dashboard**: `http://your-domain:3000`
- **Prometheus Metrics**: `http://your-domain:9090`
- **Application Health**:
    - Bot: `http://your-domain:8080/actuator/health`
    - Scrapper: `http://your-domain:8081/actuator/health`

## 🧪 Тестирование

### Запуск тестов
```bash
# Все тесты
mvn test

# Тесты конкретного модуля
mvn test -pl bot
mvn test -pl scrapper

# Интеграционные тесты
mvn verify -Pintegration-tests
```

### Анализ качества кода
```bash
# SpotBugs анализ
mvn spotbugs:check

# PMD анализ
mvn pmd:check

# Checkstyle
mvn checkstyle:check
```

## 📊 Мониторинг и метрики

Система включает comprehensive мониторинг:

### Ключевые метрики:
- **Application Health**: Статус сервисов
- **Performance**: Время отклика, throughput
- **Business Metrics**: Количество обработанных ссылок, активных пользователей
- **Infrastructure**: Использование CPU, памяти, диска
- **Database**: Производительность запросов, размер БД

### Алерты:
- Недоступность сервисов
- Высокое время отклика (>5s)
- Ошибки в обработке ссылок
- Превышение лимитов ресурсов

## 🔒 Безопасность

### Реализованные меры безопасности:
- **Rate Limiting**: Защита от DDoS атак
- **Input Validation**: Валидация всех входящих данных
- **Secure Configuration**: Использование переменных окружения для секретов
- **Database Security**: Подготовленные запросы, защита от SQL-инъекций
- **API Security**: Аутентификация и авторизация запросов

## 🤝 Contributing

### Процесс разработки:
1. Создайте feature branch от `main`
2. Внесите изменения с соблюдением code style
3. Добавьте тесты для новой функциональности
4. Убедитесь, что все проверки проходят: `mvn verify`
5. Создайте Pull Request с подробным описанием

### Code Style:
- Используйте предоставленные конфигурации Checkstyle, PMD, SpotBugs
- Следуйте принципам SOLID и Clean Code
- Покрытие тестами должно быть не менее 80%

## 📚 Дополнительные ресурсы

- [HELP.md](./HELP.md) - Подробная техническая документация
- [Architecture Decision Records](./docs/adr/) - Архитектурные решения
- [API Documentation](./docs/api/) - Детальное описание API
- [Deployment Guide](./docs/deployment/) - Руководство по развертыванию

## 📞 Поддержка

При возникновении вопросов или проблем:
1. Проверьте [Issues](../../issues) на GitHub
2. Изучите логи приложений: `docker-compose logs -f`
3. Обратитесь к команде разработки

---

**Версия**: 1.0  
**Последнее обновление**: 2025-08-18  
**Статус**: Active Development
