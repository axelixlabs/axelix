variable "env" {
  description = "The environment of the"
  type = string
  default = "test"
}

variable "folder_id" {
  description = "The id of the folder in the Yandex Cloud where infra is deployed"
  type = string
  default = "b1gkje2gk578fuj4hspq"
}

variable "default_az" {
  description = "The default availability zone to be used"
  type = string
  default = "ru-central1-d"
}

variable "k8s_version" {
  type = string
  default = "1.30"
  description = "K8S default version"
}