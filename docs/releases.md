<!--
  SPDX-FileCopyrightText: 2026 Abraham Menéndez
  SPDX-License-Identifier: AGPL-3.0-or-later
-->

# Releasing

Espeyu ships through Google Play only, and merging a pull request to `main` is
the release. Everything past the internal track is a deliberate step.

## On merge

The `Release` workflow runs `Verify`, then semantic-release, then the publish
job. semantic-release reads the squash-merged pull request titles, works out the
version, tags it and publishes the GitHub release. The publish job signs the
bundle with the upload key and runs `publishReleaseApps`, which uploads the
bundle and the store listing to the internal track.

`feat` makes a minor release and `fix`, `revert` and `chore` a patch, so every
merge reaches the internal track. Nobody but the internal testers sees it until
someone promotes it.

## Promoting a build

Run the `Promote` workflow from the Actions tab and choose `alpha`, the closed
test, or `production`. It moves the newest internal build rather than building
one, so what reaches production is the bundle the testers had.

## Release notes and store listing

[`release-notes/en-GB/default.txt`](../app/src/main/play/release-notes/en-GB/default.txt)
is what Play shows for the next upload, up to 500 characters. Update it in the
pull request that changes something a user would notice.

[`listings/en-GB/`](../app/src/main/play/listings/en-GB) holds the title, the
descriptions and the graphics, and every release publishes them, so an edit made
in the Play Console is overwritten by the next merge. Play takes an icon of
512x512, a feature graphic of 1024x500, and screenshots between 320 and 3840 px
a side whose long side is at most twice the short one.

## One-time setup

### The robot account

Google Cloud Shell, once:

```sh
PROJECT_ID=espeyu-release  # project ids are global, so take another if this one is gone
REPO=abrahammenendez/espeyu

gcloud projects create "$PROJECT_ID"
gcloud config set project "$PROJECT_ID"
gcloud services enable androidpublisher.googleapis.com iamcredentials.googleapis.com

gcloud iam service-accounts create espeyu-release --display-name="Espeyu release"

gcloud iam workload-identity-pools create github \
  --location=global --display-name="GitHub Actions"

# The condition is what stops another repository minting tokens for this account.
gcloud iam workload-identity-pools providers create-oidc espeyu \
  --location=global \
  --workload-identity-pool=github \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository == '$REPO'"

PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')

gcloud iam service-accounts add-iam-policy-binding \
  "espeyu-release@$PROJECT_ID.iam.gserviceaccount.com" \
  --role=roles/iam.workloadIdentityUser \
  --member="principalSet://iam.googleapis.com/projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/github/attribute.repository/$REPO"

echo "projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/github/providers/espeyu"
```

GitHub trades its own token for a short-lived one belonging to that account, so
no key file exists to leak or rotate.

### Play Console

Users and permissions, Invite new user, the service account's email address.
For Espeyu, grant it "Release apps to testing tracks", "Release to production,
exclude devices, and use Play App Signing" and "Manage store presence".

### GitHub

Settings, Environments, an environment named `prod` with a deployment branch rule
for `main`. It holds two secrets and two variables:

```sh
base64 < ~/keystores/espeyu-upload.jks | gh secret set UPLOAD_KEYSTORE --env prod
gh secret set UPLOAD_KEYSTORE_PASSWORD --env prod
gh variable set GOOGLE_WORKLOAD_IDENTITY_PROVIDER --env prod  # the line the block above printed
gh variable set GOOGLE_SERVICE_ACCOUNT --env prod             # espeyu-release@espeyu-release.iam.gserviceaccount.com
```

### The first upload

The Play API cannot create an app's first release, so the first bundle went up
by hand through the Console. Nothing since has.
