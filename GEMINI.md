# Project Instructions (GEMINI.md)

This file contains foundational mandates for Gemini CLI when working in this workspace. These instructions take precedence over general defaults.

## Tech Stack & Architecture
- **Primary Platforms:** Android development is the core focus.
- **Frameworks:** 
  - **Flutter:** For cross-platform mobile development.
  - **Jetpack Compose:** For native Android UI development.
- **Design System:** Rigorously follow **Material 3 Expressive** design language. Ensure components, typography, and motion align with the latest Material 3 guidelines.
- **Native Mandate:** The Jetpack Compose version must be as **native** as possible. 
  - Prioritize native Android primitives over cross-platform abstractions.
  - Use `Canvas` for custom visualizations.
  - Use `StateFlow` and `CollectAsStateWithLifecycle` for state management.
  - Leverage Room for offline-first data persistence.
  - Ensure high performance (60-120 FPS) by following native profiling and optimization standards.

## Resource & Performance Mandates
- **No Local Builds:** NEVER run `./gradlew assembleDebug`, `flutter build apk`, or any command that performs a full APK/binary build locally. The host machine is limited to **4 GB RAM**.
- **Remote Verification:** Use GitHub Actions (CI/CD) for all build verifications and APK generation.
- **Optimization Priority:** Proactively suggest and apply optimizations to ensure a smooth development experience:
  - Enable Gradle caching and configuration on demand.
  - Optimize `gradle.properties` for low-memory environments (e.g., limiting daemon memory).
  - Use specific linting and static analysis tools instead of full builds to find errors.
  - Suggest modularization to reduce the scope of analysis.

## Research & Knowledge
- **Search First:** Always utilize **Google Search (Internet)** to find the latest documentation, best practices, and troubleshooting information. Do not rely solely on internal knowledge for rapidly evolving frameworks like Flutter and Jetpack Compose.
- **Design Source of Truth:** Use **https://m3.material.io/** as the absolute authority for all design choices. Rigorously analyze its guidelines for color, typography, motion, and components to ensure a premium Material 3 Expressive experience.

## Coding & Editing Standards
- **Surgical Precision:** Adhere to the **Surgical Precise** method for all code modifications.
  - **Minimal Edits:** Modify only the specific lines that require changes.
  - **No Block Deletions:** Never delete an entire block or function to change a single line within it.
  - **Preserve Context:** Maintain surrounding formatting, comments, and structure exactly as they are.
- **Idiomatic Quality:** Ensure all changes are idiomatic to the respective framework (Dart/Flutter or Kotlin/Compose).
- **Validation:** Always verify changes with appropriate tests or build checks after implementation.

## context-mode — MANDATORY routing rules
context-mode MCP tools available. Rules protect context window from flooding.

### Think in Code — MANDATORY
Analyze/count/filter/compare/search/parse/transform data: **write code** via `mcp__context-mode__ctx_execute(language, code)`, `console.log()` only the answer. Do NOT read raw data into context. PROGRAM the analysis, not COMPUTE it. One script replaces ten tool calls.

### Tool Selection
1. **GATHER**: `mcp__context-mode__ctx_batch_execute(commands, queries)` — ONE call replaces 30+.
2. **FOLLOW-UP**: `mcp__context-mode__ctx_search(queries: ["q1", "q2", ...])` — batch all questions.
3. **PROCESSING**: `mcp__context-mode__ctx_execute(language, code)` | `mcp__context-mode__ctx_execute_file(path, language, code)` — sandbox, only stdout enters context.
4. **WEB**: `mcp__context-mode__ctx_fetch_and_index(url, source)` then `mcp__context-mode__ctx_search(queries)`.
5. **INDEX**: `mcp__context-mode__ctx_index(content, source)` — store in FTS5 for later search.

### Parallel I/O batches
Include `concurrency: N` (4-8) for network/API calls. Keep at 1 for CPU-bound or stateful work.

## Project Tracking & Documentation
- **Version.md Mandate:** Every project must contain a `Version.md` file. This file is the absolute source of truth for the project's evolution.
  - **Exhaustive Logging:** Track every changelog entry, library, method, and file version.
  - **CRITICAL: Strict Append Pattern:** 
    - **NEVER** use the `replace` tool or any method to edit, delete, or "update" existing lines in `Version.md`.
    - **ONLY** use `write_file` (with the full content) or append methods to add new entries at the bottom.
    - **NO STATUS UPDATES:** Do not update the "Status" line of a previous entry. If a status changes (e.g., from 90% to 100%), append a NEW entry with the new status.
    - **Immutable History:** Once information is written, it is permanent. The only exception is updating a library's version number within the "Libraries & Tools" section.
  - **Real-time Updates:** Log changes immediately after completion.
  - **Metadata:** Every entry must be logged with the current date and time.

## Project Commands
- **NEVER** run "/gradlew assembleDebug"

## Repository Fork Lineage & Upstream Sources
- **Gboard Patches (`D:\code\Gboard-patches`):** Forked from [jasonwu1994/Gboard-patches](https://github.com/jasonwu1994/Gboard-patches).
- **Vector Drawable (`D:\code\vector-drawable-nextjs`):** Forked from [seanghay/vector-drawable-nextjs](https://github.com/seanghay/vector-drawable-nextjs).
- **Rivo Phone App (`D:\code\RivoPhoneApp`):** Forked from [user-grinch/RivoPhoneApp](https://github.com/user-grinch/RivoPhoneApp).
