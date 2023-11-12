const std = @import("std");
const fs = std.fs;

pub fn main() !void {
    var gpa = std.heap.GeneralPurposeAllocator(.{}){};
    defer _ = gpa.deinit();
    var arena = std.heap.ArenaAllocator.init(gpa.allocator());
    defer arena.deinit();

    var tests = try fs.cwd().openDir("tests", .{});
    defer tests.close();
    const workspace_name = "hello";
    tests.deleteTree(workspace_name) catch |e| if (e != error.FileNotFound) return e;
    var workspace = try tests.makeOpenPath(workspace_name, .{});
    defer workspace.close();
    try initializeWorkspace(arena.allocator(), workspace);
}

test "initialization" {
    const name = "inittest";
    var gpa = std.heap.GeneralPurposeAllocator(.{}){};
    defer _ = gpa.deinit();
    try @import("initializer.zig").initialize(gpa.allocator(), name, name);
}

fn initializeWorkspace(allocator: std.mem.Allocator, workspace_directory: fs.Dir) !void {
    var name = try workspace_directory.realpathAlloc(allocator, ".");
    var i = name.len;
    while (i != 0) : (i -= 1)
        if (name[i - 1] == '\\')
            break;
    name = offset(u8, name, i);
    var package_path = try allocator.alloc(u8, "src/".len + name.len);
    std.mem.copy(u8, package_path, "src/");
    std.mem.copy(u8, offset(u8, package_path, "src/".len), name);
    var package_directory = try workspace_directory.makeOpenPath(package_path, .{});
    defer package_directory.close();
    var main_file = try package_directory.createFile("main.duru", .{});
    defer main_file.close();
    try main_file.writeAll("entrypoint {}\n");
}

fn offset(comptime Element: type, slice: []Element, amount: usize) []Element {
    var mutable = slice;
    mutable.ptr += amount;
    mutable.len -= amount;
    return mutable;
}
