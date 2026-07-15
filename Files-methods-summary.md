# `java.nio.file.Files` 方法总结（JDK 17）

本文总结 `java.nio.file.Files` 类中的方法。`Files` 是一个不可实例化的工具类，提供文件、目录、符号链接、文件属性、文件树遍历、便捷读写和 Stream API 等静态操作。大多数公开方法最终会委托给 `Path` 所属文件系统的 `FileSystemProvider` 执行实际 I/O。

> 范围：基于 OpenJDK 17 `java.base/java/nio/file/Files.java`。本文覆盖公开静态方法及源码中的主要私有辅助方法/内部类方法，不复制源码实现。

## 目录

- [总体设计](#总体设计)
- [公开 API 方法](#公开-api-方法)
  - [打开输入/输出流与字节通道](#打开输入输出流与字节通道)
  - [目录流](#目录流)
  - [创建与删除](#创建与删除)
  - [复制与移动](#复制与移动)
  - [符号链接、文件存储与文件比较](#符号链接文件存储与文件比较)
  - [内容类型探测](#内容类型探测)
  - [文件属性](#文件属性)
  - [文件类型判断、时间与大小](#文件类型判断时间与大小)
  - [存在性与访问权限](#存在性与访问权限)
  - [递归遍历](#递归遍历)
  - [便捷文本/字节读写](#便捷文本字节读写)
  - [Stream API](#stream-api)
- [私有辅助方法与内部类方法](#私有辅助方法与内部类方法)
- [使用注意事项](#使用注意事项)

## 总体设计

`Files` 的核心特点：

1. **静态工具类**：构造器为私有，不能创建实例。
2. **Provider 委托模型**：大多数方法通过 `path.getFileSystem().provider()` 委托给对应文件系统实现。
3. **支持多文件系统**：当源路径和目标路径属于不同 provider 时，复制/移动会走跨 provider 辅助逻辑。
4. **大量重载**：常见重载会默认使用 UTF-8、默认打开选项、默认遍历深度等。
5. **I/O 异常语义明确**：会抛出 `IOException`、`SecurityException`、`UnsupportedOperationException`、`IllegalArgumentException` 等。
6. **部分布尔判断吞掉 I/O 异常**：如 `exists`、`isDirectory`、`isRegularFile` 等在无法确定时通常返回 `false`。
7. **Stream 资源必须关闭**：`list`、`walk`、`find`、`lines` 返回的 `Stream` 持有打开的目录或文件，应使用 try-with-resources。

---

## 公开 API 方法

### 打开输入/输出流与字节通道

| 方法 | 作用 | 关键点 |
|---|---|---|
| `newInputStream(Path path, OpenOption... options)` | 打开文件输入流。 | 默认等价于 READ；返回流通常不带缓冲；具体异步关闭/中断行为依 provider 而定。 |
| `newOutputStream(Path path, OpenOption... options)` | 打开或创建文件输出流。 | 无选项时默认 CREATE、TRUNCATE_EXISTING、WRITE；不能包含 READ。 |
| `newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)` | 打开或创建可定位字节通道。 | 支持 READ/WRITE/APPEND/CREATE 等选项；可在创建时原子设置属性。 |
| `newByteChannel(Path path, OpenOption... options)` | 使用可变参数打开可定位字节通道。 | 将可变参数转换为 `Set` 后调用上一个重载；无选项时默认读。 |

### 目录流

| 方法 | 作用 | 关键点 |
|---|---|---|
| `newDirectoryStream(Path dir)` | 打开目录并遍历所有条目。 | 返回 `DirectoryStream<Path>`；需要关闭。 |
| `newDirectoryStream(Path dir, String glob)` | 用 glob 模式过滤目录条目。 | `"*"` 会走全部条目的快捷路径；其他模式通过 `PathMatcher` 匹配文件名。 |
| `newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)` | 用自定义过滤器遍历目录。 | 过滤器抛出的 `IOException` 会被包装成 `DirectoryIteratorException`。 |

### 创建与删除

| 方法 | 作用 | 关键点 |
|---|---|---|
| `createFile(Path path, FileAttribute<?>... attrs)` | 原子创建新空文件。 | 文件已存在则失败；创建时可原子设置属性。 |
| `createDirectory(Path dir, FileAttribute<?>... attrs)` | 创建单个目录。 | 父目录必须已存在；目标已存在通常抛异常。 |
| `createDirectories(Path dir, FileAttribute<?>... attrs)` | 创建目录及所有不存在的父目录。 | 目标目录已存在不会抛异常；失败时可能已创建部分父目录。 |
| `createTempFile(Path dir, String prefix, String suffix, FileAttribute<?>... attrs)` | 在指定目录创建临时文件。 | `prefix` 可为 `null`；`suffix` 为 `null` 时默认 `.tmp`。 |
| `createTempFile(String prefix, String suffix, FileAttribute<?>... attrs)` | 在默认临时目录创建临时文件。 | 等价于指定默认临时目录的重载。 |
| `createTempDirectory(Path dir, String prefix, FileAttribute<?>... attrs)` | 在指定目录创建临时目录。 | 用 `prefix` 生成候选目录名。 |
| `createTempDirectory(String prefix, FileAttribute<?>... attrs)` | 在默认临时目录创建临时目录。 | 等价于指定默认临时目录的重载。 |
| `createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs)` | 创建符号链接。 | 可选操作；目标可以不存在；可能受平台权限限制。 |
| `createLink(Path link, Path existing)` | 创建硬链接。 | 可选操作；通常要求同一文件系统。 |
| `delete(Path path)` | 删除文件或空目录。 | 删除符号链接本身而非目标；不存在时抛 `NoSuchFileException`。 |
| `deleteIfExists(Path path)` | 文件存在则删除。 | 不存在返回 `false`；其他 I/O 问题仍可抛异常。 |

### 复制与移动

| 方法 | 作用 | 关键点 |
|---|---|---|
| `copy(Path source, Path target, CopyOption... options)` | 复制文件或空目录到目标路径。 | 同 provider 直接委托；不同 provider 使用跨 provider 复制辅助逻辑。 |
| `move(Path source, Path target, CopyOption... options)` | 移动或重命名文件。 | 支持 `REPLACE_EXISTING`、`ATOMIC_MOVE`；不同 provider 使用跨 provider 移动辅助逻辑。 |
| `copy(InputStream in, Path target, CopyOption... options)` | 从输入流复制所有字节到文件。 | 仅要求支持 `REPLACE_EXISTING`；输入流不会由此方法关闭。 |
| `copy(Path source, OutputStream out)` | 从文件复制所有字节到输出流。 | 输出流不会由此方法关闭；调用者负责刷新/关闭。 |

### 符号链接、文件存储与文件比较

| 方法 | 作用 | 关键点 |
|---|---|---|
| `readSymbolicLink(Path link)` | 读取符号链接的目标路径。 | 目标不必存在；非符号链接通常抛 `NotLinkException`。 |
| `getFileStore(Path path)` | 获取文件所在的 `FileStore`。 | 后续文件被删除或移动后的行为由实现决定。 |
| `isSameFile(Path path, Path path2)` | 判断两个路径是否定位到同一文件。 | 相等路径直接返回 `true`；不同 provider 通常返回 `false`。 |
| `mismatch(Path path, Path path2)` | 返回两个文件内容第一个不同字节的位置。 | 完全相同返回 `-1`；大小不同但前缀相同则返回较小文件大小。 |
| `isHidden(Path path)` | 判断文件是否隐藏。 | 隐藏定义依平台/provider 而定，例如 Unix 点文件、Windows hidden 属性。 |

### 内容类型探测

| 方法 | 作用 | 关键点 |
|---|---|---|
| `probeContentType(Path path)` | 探测文件 MIME 类型。 | 先尝试已安装的 `FileTypeDetector`，再使用系统默认探测器；无法判断返回 `null`。 |

### 文件属性

| 方法 | 作用 | 关键点 |
|---|---|---|
| `getFileAttributeView(Path path, Class<V> type, LinkOption... options)` | 获取指定类型的文件属性视图。 | 不支持时返回 `null`；`NOFOLLOW_LINKS` 可控制是否跟随符号链接。 |
| `readAttributes(Path path, Class<A> type, LinkOption... options)` | 读取强类型文件属性。 | 所有实现都支持 `BasicFileAttributes`；其他属性视图可能不支持。 |
| `setAttribute(Path path, String attribute, Object value, LinkOption... options)` | 设置单个文件属性。 | 属性名格式为 `[view-name:]attribute-name`；返回原路径。 |
| `getAttribute(Path path, String attribute, LinkOption... options)` | 读取单个文件属性值。 | 不允许 `*` 或 `,`；内部调用字符串形式的 `readAttributes`。 |
| `readAttributes(Path path, String attributes, LinkOption... options)` | 按字符串表达式批量读取属性。 | 表达式格式为 `[view-name:]attribute-list`，支持 `*`。 |
| `getPosixFilePermissions(Path path, LinkOption... options)` | 获取 POSIX 文件权限集合。 | 文件系统必须支持 `PosixFileAttributeView`。 |
| `setPosixFilePermissions(Path path, Set<PosixFilePermission> perms)` | 设置 POSIX 文件权限。 | 不支持 POSIX 视图时抛 `UnsupportedOperationException`。 |
| `getOwner(Path path, LinkOption... options)` | 获取文件所有者。 | 依赖 `FileOwnerAttributeView`。 |
| `setOwner(Path path, UserPrincipal owner)` | 设置文件所有者。 | 依赖 `FileOwnerAttributeView`；返回原路径。 |

### 文件类型判断、时间与大小

| 方法 | 作用 | 关键点 |
|---|---|---|
| `isSymbolicLink(Path path)` | 判断路径是否为符号链接。 | 读取 `BasicFileAttributes` 且不跟随链接；I/O 异常时返回 `false`。 |
| `isDirectory(Path path, LinkOption... options)` | 判断路径是否为目录。 | 默认跟随符号链接；无法确定时返回 `false`。 |
| `isRegularFile(Path path, LinkOption... options)` | 判断路径是否为普通文件。 | 默认跟随符号链接；无法确定时返回 `false`。 |
| `getLastModifiedTime(Path path, LinkOption... options)` | 获取最后修改时间。 | 返回 `FileTime`；可控制是否跟随符号链接。 |
| `setLastModifiedTime(Path path, FileTime time)` | 设置最后修改时间。 | 通过 `BasicFileAttributeView#setTimes` 实现；返回原路径。 |
| `size(Path path)` | 获取文件大小，单位字节。 | 非普通文件的大小语义依实现而定。 |

### 存在性与访问权限

| 方法 | 作用 | 关键点 |
|---|---|---|
| `exists(Path path, LinkOption... options)` | 判断文件是否存在。 | 不是强一致结果；无法确定时返回 `false`。 |
| `notExists(Path path, LinkOption... options)` | 判断文件确认不存在。 | 不是 `exists` 的简单取反；无法确定时也返回 `false`。 |
| `isReadable(Path path)` | 判断文件是否可读。 | 调用 provider 的访问检查；无法确定时返回 `false`。 |
| `isWritable(Path path)` | 判断文件是否可写。 | 结果可能马上过期；无法确定时返回 `false`。 |
| `isExecutable(Path path)` | 判断文件是否可执行。 | 在 Unix 上目录的 execute 通常表示可搜索。 |

### 递归遍历

| 方法 | 作用 | 关键点 |
|---|---|---|
| `walkFileTree(Path start, Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor)` | 深度优先遍历文件树并回调 visitor。 | 支持最大深度、是否跟随符号链接、循环检测；visitor 返回值控制遍历。 |
| `walkFileTree(Path start, FileVisitor<? super Path> visitor)` | 遍历整棵文件树。 | 等价于不跟随符号链接、最大深度 `Integer.MAX_VALUE`。 |

### 便捷文本/字节读写

| 方法 | 作用 | 关键点 |
|---|---|---|
| `newBufferedReader(Path path, Charset cs)` | 创建指定字符集的缓冲字符输入流。 | 字节按 `cs` 解码；读取时遇到非法字节序列会抛 `IOException`。 |
| `newBufferedReader(Path path)` | 创建 UTF-8 缓冲字符输入流。 | 等价于指定 UTF-8 的重载。 |
| `newBufferedWriter(Path path, Charset cs, OpenOption... options)` | 创建指定字符集的缓冲字符输出流。 | 无选项时默认创建/截断/写入。 |
| `newBufferedWriter(Path path, OpenOption... options)` | 创建 UTF-8 缓冲字符输出流。 | 等价于指定 UTF-8 的重载。 |
| `readAllBytes(Path path)` | 将整个文件读入 `byte[]`。 | 不适合超大文件；文件大小超过 `Integer.MAX_VALUE` 会抛 `OutOfMemoryError`。 |
| `readString(Path path)` | 用 UTF-8 读取整个文件为字符串。 | Java 11 引入；等价于指定 UTF-8 的重载。 |
| `readString(Path path, Charset cs)` | 用指定字符集读取整个文件为字符串。 | 内部先读全部字节，再解码。 |
| `readAllLines(Path path, Charset cs)` | 用指定字符集读取所有行到 `List<String>`。 | 去除行终止符；不适合超大文件。 |
| `readAllLines(Path path)` | 用 UTF-8 读取所有行。 | 等价于指定 UTF-8 的重载。 |
| `write(Path path, byte[] bytes, OpenOption... options)` | 将字节数组写入文件。 | 无选项时默认创建/截断/写入；分块写出。 |
| `write(Path path, Iterable<? extends CharSequence> lines, Charset cs, OpenOption... options)` | 将多行文本写入文件。 | 每行后写入平台行分隔符；使用指定字符集编码。 |
| `write(Path path, Iterable<? extends CharSequence> lines, OpenOption... options)` | 用 UTF-8 写入多行文本。 | 等价于指定 UTF-8 的重载。 |
| `writeString(Path path, CharSequence csq, OpenOption... options)` | 用 UTF-8 写入整个字符序列。 | Java 11 引入；不额外添加换行。 |
| `writeString(Path path, CharSequence csq, Charset cs, OpenOption... options)` | 用指定字符集写入整个字符序列。 | 先编码为字节再写入；不额外添加换行。 |

### Stream API

| 方法 | 作用 | 关键点 |
|---|---|---|
| `list(Path dir)` | 返回目录直接子项的惰性 `Stream<Path>`。 | 非递归；Stream 持有打开目录，必须关闭。 |
| `walk(Path start, int maxDepth, FileVisitOption... options)` | 返回深度优先遍历文件树的惰性 `Stream<Path>`。 | Stream 至少包含起始路径；可能持有多个打开目录。 |
| `walk(Path start, FileVisitOption... options)` | 遍历所有深度的文件树 Stream。 | 等价于 `maxDepth = Integer.MAX_VALUE`。 |
| `find(Path start, int maxDepth, BiPredicate<Path, BasicFileAttributes> matcher, FileVisitOption... options)` | 搜索文件树并返回匹配路径 Stream。 | 比 `walk(...).filter(...)` 可更高效，因为遍历时已取得属性。 |
| `lines(Path path, Charset cs)` | 用指定字符集惰性读取文件行 Stream。 | 对 UTF-8、US-ASCII、ISO-8859-1 等可使用更优拆分实现。 |
| `lines(Path path)` | 用 UTF-8 惰性读取文件行 Stream。 | 等价于指定 UTF-8 的重载；Stream 必须关闭。 |

---

## 私有辅助方法与内部类方法

这些方法不是公开 API，但解释了 `Files` 的内部组织方式。

| 方法/位置 | 作用 | 关键点 |
|---|---|---|
| `Files()` | 私有构造器。 | 防止实例化。 |
| `provider(Path path)` | 获取路径对应的 `FileSystemProvider`。 | 大多数公开方法通过它委托实际操作。 |
| `asUncheckedRunnable(Closeable c)` | 将 `Closeable#close` 包装为 `Runnable`。 | `IOException` 会转为 `UncheckedIOException`，常用于 `Stream.onClose`。 |
| `AcceptAllFilter()` | `AcceptAllFilter` 的私有构造器。 | 目录流默认“接受全部”的过滤器。 |
| `AcceptAllFilter.accept(Path entry)` | 接受所有目录条目。 | 始终返回 `true`。 |
| `newDirectoryStream(Path dir, String glob)` 中的匿名 `Filter.accept(Path entry)` | 对目录条目执行 glob 匹配。 | 使用 `PathMatcher` 匹配 `entry.getFileName()`。 |
| `createAndCheckIsDirectory(Path dir, FileAttribute<?>... attrs)` | `createDirectories` 的辅助创建逻辑。 | 如果目录已存在则检查它确实是目录；否则继续抛异常。 |
| `FileTypeDetectors.createDefaultFileTypeDetector()` | 创建系统默认文件类型探测器。 | 在特权动作中调用平台默认探测器工厂。 |
| `FileTypeDetectors.loadInstalledDetectors()` | 加载已安装的文件类型探测器。 | 使用 `ServiceLoader<FileTypeDetector>` 从系统类加载器加载。 |
| `FileTypeDetectors` 中的匿名 `PrivilegedAction.run()` | 执行文件类型探测器创建/加载。 | 用于受控地执行需要权限的初始化逻辑。 |
| `followLinks(LinkOption... options)` | 解析是否跟随符号链接。 | 存在 `NOFOLLOW_LINKS` 返回 `false`；非法选项会触发断言错误。 |
| `isAccessible(Path path, AccessMode... modes)` | 统一执行可读/可写/可执行检查。 | 调用 provider `checkAccess`；I/O 异常返回 `false`。 |
| `read(InputStream source, int initialSize)` | 从输入流读取全部字节。 | `initialSize` 是初始容量提示；会按需扩容。 |
| `list(Path dir)` 中的匿名 `Iterator.hasNext()` | 包装目录迭代器的 `hasNext`。 | 将 `DirectoryIteratorException` 转换为 `UncheckedIOException`。 |
| `list(Path dir)` 中的匿名 `Iterator.next()` | 包装目录迭代器的 `next`。 | 同样转换目录迭代异常。 |
| `createFileChannelLinesStream(FileChannel fc, Charset cs)` | 为 `lines` 创建基于 `FileChannel` 的高效行 Stream。 | 仅在文件大小适合且字符集受支持时返回非空 Stream。 |
| `createBufferedReaderLinesStream(BufferedReader br)` | 为 `lines` 创建基于 `BufferedReader` 的行 Stream。 | 作为通用回退实现；关闭 Stream 时关闭 reader。 |

---

## 使用注意事项

1. **`exists` 与 `notExists` 不是互补关系**：无法确定时二者都可能返回 `false`。
2. **布尔判断结果会过期**：`exists`、`isReadable`、`isWritable` 等结果只反映检查时刻，随后文件状态可能变化。
3. **Stream 要及时关闭**：`list`、`walk`、`find`、`lines` 应写在 try-with-resources 中。
4. **大文件避免一次性读取**：`readAllBytes`、`readString`、`readAllLines` 会把内容加载进内存。
5. **符号链接行为要明确**：很多方法默认跟随符号链接，可通过 `LinkOption.NOFOLLOW_LINKS` 改变行为。
6. **跨文件系统操作不一定原子**：`copy`、`move` 在不同 provider 或不同 `FileStore` 时可能退化为非原子操作。
7. **文件属性支持依赖文件系统**：POSIX、owner、DOS 等属性视图不是所有平台都支持。
8. **临时文件/目录仍需清理策略**：临时对象不会自动保证删除，可结合 `DELETE_ON_CLOSE`、shutdown hook 或应用清理逻辑。

