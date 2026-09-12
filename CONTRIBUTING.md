# Contributing to Espeyu

Bug reports, ideas and pull requests are welcome. Espeyu is deliberately small,
so read the non-goals before proposing a feature.

## Non-goals

These are binding. A change that needs any of them is declined, however well it
is made.

- Photo capture, video, saving or sharing.
- Filters.
- Accounts, ads, in-app purchases or analytics.
- Anything that needs the `INTERNET` permission.
- Dependencies or features that make the app slower or bigger, without
  removing something in return.

## Issues

Use the bug report or feature request form. Report a security problem
privately, as [`SECURITY.md`](SECURITY.md) describes, never in a public issue.

## Pull requests

For anything beyond a small fix, open an issue first, so nobody spends time on
a change that will not be merged.

- [Getting started](README.md#getting-started) covers the build and the checks
  CI runs. Run them before you push.
- If a screen looks different after your change, record its golden screenshots
  again, as [Testing](README.md#testing) describes.
- Every file carries SPDX headers or is annotated in `REUSE.toml`.

Pull requests are squash merged, so the commits on your branch can say anything:
the title becomes the one commit on `main`. CI checks it as a
[Conventional Commit](https://www.conventionalcommits.org/en/v1.0.0/).

- The type is `feat`, `fix`, `revert` or `chore`. Documentation, refactoring,
  tests, formatting, performance and CI changes are all `chore`. Every merge
  is a release, as [Releasing](README.md#releasing) describes.
- Type, scope and subject are lowercase, with no trailing period.

## Licence

Contributions are accepted under the project's licence, AGPL-3.0-or-later.
