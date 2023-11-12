const std = @import("std");
const windows = @cImport(@cInclude("windows.h"));

pub fn initialize(
    allocator: std.mem.Allocator,
    directory: []const u8,
    name: []const u8,
) !void {
    _ = name;
    _ = directory;
    _ = allocator;
    windows.MAX_PATH;
}

fn store(
    allocator: std.mem.Allocator,
    file: []const u8,
    format: []const u8,
    arguments: anytype,
) !void {
    const path = std.fs.realpathAlloc(allocator, file);
    defer allocator.free(path);
    const handle = try std.fs.openFileAbsolute(path, .{});
    defer handle.close();
    const text = try std.fmt.allocPrint(allocator, format, arguments);
    defer allocator.free(text);
    try handle.writeAll(text);
}
