typedef struct FILE FILE;
int fputs(char* string, FILE* destination);
FILE* __acrt_iob_func(unsigned index);
FILE* duru$get_error_stream() {
  return __acrt_iob_func(2);
}
[[noreturn]] void exit(int code);
char duru$print(char* string) {
  int result = fputs(string, duru$get_error_stream());
  if (result < 0) {
    exit(result);
  }
  return 0;
}
char walkthrough$main() {
  duru$print("Hello, World!\n");
  return 0;
}
int main() {
  walkthrough$main();
}
