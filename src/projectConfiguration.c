#include "internal.h"

#include <stdbool.h>
#include <stdio.h>

typedef enum DuruTokenType DuruTokenType;
typedef struct DuruToken   DuruToken;
typedef struct DuruParser  DuruParser;

enum DuruTokenType {
    duruDot,
    duruSemicolon,
    duruOpeningBrace,
    duruClosingBrace,
    duruProjectKeyword,
    duruExecutableKeyword,
    duruIdentifier,
};

struct DuruToken {
    DuruStringView text;
    DuruTokenType  type;
};

struct DuruParser {
    DuruProjectConfiguration* configuration;
    DuruStringView            contents;
    size_t                    nextByte;
    size_t                    currentByte;
    size_t                    initialByte;
    int                       currentCharacter;
    int                       initialCharacter;
    DuruToken                 lastToken;
};

static void           duruParse(DuruParser* parser);
static DuruStringView duruParsePackage(DuruParser* parser);
static bool           duruHas(DuruParser* parser);
static void           duruAdvance(DuruParser* parser);
static void duruInitializeToken(DuruParser* parser, DuruTokenType type);
static bool duruLex(DuruParser* parser);
static void duruEnsureToken(DuruParser* parser, DuruTokenType type);
static bool duruIsWhitespace(int character);
static bool duruIsLetter(int character);
static bool duruIsWord(int character);

void duruDestroyProjectConfiguration(DuruProjectConfiguration configuration) {
    duruDestroyStringList(configuration.executables);
    duruDestroyStringList(configuration.exports);
}

void duruParseProjectConfiguration(
  DuruProjectConfiguration* configuration, DuruStringView contents) {
    DuruParser parser = {.configuration = configuration, .contents = contents};
    duruParse(&parser);
}

static void duruParse(DuruParser* parser) {
    duruEnsureToken(parser, duruProjectKeyword);
    duruEnsureToken(parser, duruIdentifier);
    parser->configuration->name = parser->lastToken.text;
    duruEnsureToken(parser, duruOpeningBrace);
    duruEnsure(
      duruLex(parser),
      "Expected the project definition closer `}` at the configuration file's end!");
    while (parser->lastToken.type != duruClosingBrace) {
        switch (parser->lastToken.type) {
            case duruExecutableKeyword:
                DuruStringView package = duruParsePackage(parser);
                duruEnsureToken(parser, duruSemicolon);
                duruPushString(&parser->configuration->executables, package);
                printf(
                  "Executable: `%.*s`!\n", (int)package.size, package.bytes);
                break;
            default:
                duruFail(
                  "Expected the project definition closer `}` in the configuration file instead of `%i`!",
                  parser->lastToken.type);
        }
        duruEnsure(
          duruLex(parser),
          "Expected the project definition closer `}` at the configuration file's end!");
    }
}

static DuruStringView duruParsePackage(DuruParser* parser) {
    duruEnsureToken(parser, duruIdentifier);
    DuruStringView package = parser->lastToken.text;
    return package;
}

static bool duruHas(DuruParser* parser) {
    return parser->nextByte != parser->contents.size;
}

static void duruAdvance(DuruParser* parser) {
    parser->currentByte = parser->nextByte;
    parser->currentCharacter =
      duruDecodeCharacter(parser->contents, &parser->nextByte);
    printf("%c", parser->currentCharacter);
}

static void duruInitializeToken(DuruParser* parser, DuruTokenType type) {
    parser->lastToken.text.bytes = parser->contents.bytes + parser->initialByte;
    parser->lastToken.text.size  = parser->nextByte - parser->initialByte;
    parser->lastToken.type       = type;
}

static bool duruLex(DuruParser* parser) {
    do {
        if (!duruHas(parser)) { return false; }
        duruAdvance(parser);
        parser->initialByte      = parser->currentByte;
        parser->initialCharacter = parser->currentCharacter;
    } while (duruIsWhitespace(parser->initialCharacter));
    switch (parser->initialCharacter) {
        case '.': duruInitializeToken(parser, duruDot); break;
        case ';': duruInitializeToken(parser, duruSemicolon); break;
        case '{': duruInitializeToken(parser, duruOpeningBrace); break;
        case '}': duruInitializeToken(parser, duruClosingBrace); break;
        default:
            if (duruIsLetter(parser->initialCharacter)) {
                while (duruHas(parser)) {
                    if (!duruIsWord(parser->currentCharacter)) {
                        parser->nextByte = parser->currentByte;
                        break;
                    }
                    duruAdvance(parser);
                }
                duruInitializeToken(parser, duruIdentifier);
                if (
                  duruCompare(parser->lastToken.text, duruView("project"))
                  == 0) {
                    parser->lastToken.type = duruProjectKeyword;
                    break;
                }
                if (
                  duruCompare(parser->lastToken.text, duruView("executable"))
                  == 0) {
                    parser->lastToken.type = duruExecutableKeyword;
                    break;
                }
                break;
            }
            duruFail(
              "Unknown character `%c` in the configuration file at index `%zu`!",
              parser->initialCharacter,
              parser->initialByte);
    }
    printf(
      "-> Lexed `%.*s` as `%i`!\n",
      (int)parser->lastToken.text.size,
      parser->lastToken.text.bytes,
      parser->lastToken.type);
    return true;
}

static void duruEnsureToken(DuruParser* parser, DuruTokenType type) {
    duruEnsure(
      duruLex(parser),
      "Expected a token of type `%i` at the configuration file's end!",
      type);
    duruEnsure(
      parser->lastToken.type == type,
      "Expected a token of type `%i` in the configuration file instead of `%i`!",
      type,
      parser->lastToken.type);
}

static bool duruIsWhitespace(int character) {
    return character == ' ' || character == '\t' || character == '\n';
}

static bool duruIsLetter(int character) {
    return (character >= 'a' && character <= 'z')
        || (character >= 'A' && character <= 'Z');
}

static bool duruIsWord(int character) {
    return duruIsLetter(character) || (character >= '0' && character <= '9');
}
