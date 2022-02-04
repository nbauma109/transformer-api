# Transformer API
[![](https://jitpack.io/v/nbauma109/transformer-api.svg)](https://jitpack.io/#nbauma109/transformer-api)
[![](https://jitci.com/gh/nbauma109/transformer-api/svg)](https://jitci.com/gh/nbauma109/transformer-api)
[![CodeQL](https://github.com/nbauma109/transformer-api/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/nbauma109/transformer-api/actions/workflows/codeql-analysis.yml)

The Transformer API provides convenient access to different transformers (currently decompilers only) under a unified
API. The API is still subject to major changes, but only with a major version bump.

## Usage

Currently, this API supports 3 decompilers. They are:

- Fernflower
- Procyon
- CFR

Decompilers can be accessed either via `StandardTransformers.DECOMPILER` or by creating a new instance. They are also
stateless, which means you can use the same instance across different threads.

## Features

### Updates

This API will be updated as decompilers receive updates, which means fixes reach you faster.
