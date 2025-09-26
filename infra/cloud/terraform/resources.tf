resource "yandex_container_registry" "registry" {
  name      = "${var.env}-registry"
  folder_id = var.folder_id
}

resource "yandex_container_repository" "master-repository" {
  name = "${yandex_container_registry.registry.id}/master"
}

resource "yandex_container_repository" "front-end-repository" {
  name = "${yandex_container_registry.registry.id}/front-end"
}

resource "yandex_container_repository" "playground-repository" {
  name = "${yandex_container_registry.registry.id}/playground"
}

resource "yandex_vpc_network" "k8s_network" {
  name = "${var.env}-k8s"
  description = "Main ${var.env} network"
  folder_id = var.folder_id

  labels = {
    "env" = var.env
  }
}

resource "yandex_vpc_subnet" "k8s_subnet" {
  name = "k8s_subnet"
  network_id = yandex_vpc_network.k8s_network.id
  v4_cidr_blocks = ["10.0.0.0/22"]
  folder_id = var.folder_id
  zone = var.default_az

  labels = {
    "env" = var.env
  }
}

resource "yandex_iam_service_account" "terraform" {
  name = "terraform-user"
  description = "Tech user under which the terraform deploys infrastructure"
  folder_id = var.folder_id
}

resource "yandex_container_registry_iam_binding" "terraform_cr_access" {
  members = [ "serviceAccount:${yandex_iam_service_account.terraform.id}" ]
  registry_id = yandex_container_registry.registry.id
  role        = "admin"
}

resource "yandex_iam_service_account_static_access_key" "terraform-static-access-key" {
  service_account_id = yandex_iam_service_account.terraform.id
  description        = "The general pair os secret/access key to access various resources"
}

resource "yandex_vpc_security_group" "k8s_security_group" {
  network_id = yandex_vpc_network.k8s_network.id
  folder_id = var.folder_id
  description = "HTTP/HTTPS from anywhere allowed security group"
  name = "web-from-anywhere"

  ingress {
    protocol = "TCP"
    port = 80
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    protocol = "TCP"
    port = 443
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    protocol = "TCP"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "yandex_kubernetes_cluster" "main" {
  name        = var.env
  description = "K8S cluster for ${var.env} env"

  network_id = yandex_vpc_network.k8s_network.id

  master {
    version = "1.30"

    zonal {
      zone      = yandex_vpc_subnet.k8s_subnet.zone
      subnet_id = yandex_vpc_subnet.k8s_subnet.id
    }

    public_ip = true

    security_group_ids = [yandex_vpc_security_group.k8s_security_group.id]

    maintenance_policy {
      auto_upgrade = true

      maintenance_window {
        start_time = "22:00"
        duration   = "3h"
      }
    }

    master_logging {
      enabled                    = false
      kube_apiserver_enabled     = false
      cluster_autoscaler_enabled = false
      events_enabled             = false
      audit_enabled              = false
    }
  }

  service_account_id      = yandex_iam_service_account.terraform.id

  # TODO:
  #  Technically we might want to use here a different service account, but we go with
  #  the one that provisions the infrastructure for now
  node_service_account_id = yandex_iam_service_account.terraform.id

  labels = {
    env = var.env
  }

  release_channel         = "RAPID"
  network_policy_provider = "CALICO"
}

resource "yandex_kubernetes_node_group" "my_node_group" {
  cluster_id  = yandex_kubernetes_cluster.main.id
  name        = "default"
  description = "Default node group"
  version     = var.k8s_version

  labels = {
    "env" = var.env
  }

  instance_template {
    platform_id = "standard-v2"

    network_interface {
      nat        = true
      subnet_ids = [yandex_vpc_subnet.k8s_subnet.id]
    }

    resources {
      memory = 4
      cores  = 2
    }

    boot_disk {
      type = "network-hdd"
      size = 64
    }

    scheduling_policy {
      preemptible = false
    }

    container_runtime {
      type = "containerd"
    }
  }

  scale_policy {
    fixed_scale {
      size = 1
    }
  }

  allocation_policy {
    location {
      zone = var.default_az
    }
  }

  maintenance_policy {
    auto_upgrade = true
    auto_repair  = true

    maintenance_window {
      day        = "monday"
      start_time = "15:00"
      duration   = "3h"
    }

    maintenance_window {
      day        = "friday"
      start_time = "10:00"
      duration   = "4h30m"
    }
  }
}