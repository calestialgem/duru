typedef char duru$Unit;
typedef char duru$Byte;
typedef int duru$Integer32;
typedef struct duru$Stream duru$Stream;
duru$Integer32 fputs(duru$Byte* string, duru$Stream* destination);
typedef unsigned duru$Natural32;
duru$Stream* __acrt_iob_func(duru$Natural32 index);
duru$Stream* duru$get_error_stream() {
  return __acrt_iob_func(2);
}
#define duru$Noreturn [[noreturn]] void
duru$Noreturn exit(duru$Integer32 code);
duru$Unit duru$print(duru$Byte* string) {
  duru$Integer32 result = fputs(string, duru$get_error_stream());
  if (result < 0) {
    exit(result);
  }
}
duru$Unit testmodule$main() {
  duru$print("Hello, World!\n");
}
