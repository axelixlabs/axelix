#!/bin/bash

set -e

# TODO:
#  The token issued by the yc cli is pretty short-lived, so it is unclear how are we going to
#  extract this into CI/CD pipeline. We could, theoretically, either install the yc cli on the
#  build agent itself and authenticate, or we can explore other ways of authentication. But for
#  now, we can just assume we're going to deploy the infra locally.
if [[ -z $YC_TOKEN ]]; then
  export YC_TOKEN=$(yc iam create-token)
fi

if [[ -z $YC_CLOUD_ID ]]; then
  export YC_CLOUD_ID=$(yc config get cloud-id)
fi

if [[ -z $YC_FOLDER_ID ]]; then
  export YC_FOLDER_ID=$(yc config get folder-id)
fi

if [[ -z $ACCESS_KEY || -z $SECRET_KEY ]]; then
  echo "[ERROR] The ACCESS_KEY and SECRET_KEY env variables must be set in order to access the TF state in cloud storage"
  exit 1
fi

export TF_CLI_CONFIG_FILE="$(realpath $(dirname $0))/.terraformrc"
terraform init -backend-config="access_key=$ACCESS_KEY" -backend-config="secret_key=$SECRET_KEY"
terraform apply
