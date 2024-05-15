terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
}

variable "yandex_oauth_token" {
  type = string
}

variable "yandex_cloud_id" {
  type = string
}

variable "yandex_folder_id" {
  type = string
}

variable "yandex_zone" {
  type = string
}

variable "bot_token" {
  type = string
}

variable "bot_name" {
  type = string
}

variable "db_name" {
  type = string
}

variable "db_user" {
  type = string
}

variable "db_password" {
  type = string
}

provider "yandex" {
  token     = var.yandex_oauth_token
  cloud_id  = var.yandex_cloud_id
  folder_id = var.yandex_folder_id
  zone      = var.yandex_zone
}

# Data source для создания образа с Docker. Нужен для того, чтобы впоследствии разворачивать на этом образе
# свои контейнеры.
data "yandex_compute_image" "container-optimized-image" {
  family = "container-optimized-image"
}

resource "yandex_vpc_network" "weather-bot-network" {
  name = "weather-bot-network"
}

resource "yandex_vpc_subnet" "weather-bot-subnet" {
  name           = "weather-bot-subnet"
  zone           = "ru-central1-a"
  network_id     = yandex_vpc_network.weather-bot-network.id
  v4_cidr_blocks = ["10.2.0.0/16"]
}

resource "yandex_mdb_postgresql_cluster" "weather-bot-postgres" {
  name        = "weather-bot-postgres"
  environment = "PRODUCTION"
  network_id  = yandex_vpc_network.weather-bot-network.id

  config {
    version = 12
    resources {
      resource_preset_id = "s2.micro"
      disk_type_id       = "network-ssd"
      disk_size          = 30
    }
    postgresql_config = {
      max_connections                   = 395
      enable_parallel_hash              = true
      vacuum_cleanup_index_scale_factor = 0.2
      autovacuum_vacuum_scale_factor    = 0.34
      default_transaction_isolation     = "TRANSACTION_ISOLATION_READ_COMMITTED"
      shared_preload_libraries          = "SHARED_PRELOAD_LIBRARIES_AUTO_EXPLAIN,SHARED_PRELOAD_LIBRARIES_PG_HINT_PLAN"
    }
  }

  database {
    name  = var.db_name
    owner = var.db_user
  }

  user {
    name       = var.db_user
    password   = var.db_password
    conn_limit = 50
    permission {
      database_name = var.db_name
    }
    settings = {
      default_transaction_isolation = "read committed"
      log_min_duration_statement    = 5000
    }
  }

  host {
    zone      = "ru-central1-a"
    subnet_id = yandex_vpc_subnet.weather-bot-subnet.id
  }
}

resource "yandex_compute_instance" "weather-bot-vm" {
  name        = "weather-bot-vm"
  platform_id = "standard-v1"
  zone        = "ru-central1-a"

  resources {
    cores  = 2
    memory = 2
  }

  boot_disk {
    initialize_params {
      image_id = data.yandex_compute_image.container-optimized-image.id
    }
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.weather-bot-subnet.id
    nat = true
  }

  # Необходимо для того, чтобы запуск Docker - контейнера происходил только после создания базы данных.
  # Если создать контейнер с приложением до создания БД и получения её адреса, то приложение будет
  # падать при запуске и не работать.
  depends_on = [yandex_mdb_postgresql_cluster.weather-bot-postgres]

  metadata = {
    # Конфигурация Docker - контейнера. В её шаблон подставляются необходимые переменные, такие как
    # fqdn адрес БД, логин и пароль для доступа к ней, а также токен бота Telegram.
    docker-container-declaration = templatefile("${path.module}/declaration.tftpl", {
      bot_name      = var.bot_name,
      bot_token     = var.bot_token,
      db_name       = var.db_name,
      db_user       = var.db_user,
      db_password   = var.db_password,
      postgres_fqdn = yandex_mdb_postgresql_cluster.weather-bot-postgres.host[0].fqdn
    })
    # Конфигурация cloud-init.
    user-data = file("${path.module}/cloud_config.yaml")
  }
}
