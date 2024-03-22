# Chadasted

A plugin that lets players choose how their messages are formatted.

## Installation

1. Clone the repo: `$ git clone https://github.com/5GameMaker/Chadasted.git --depth 1`
2. Modify file `src/main/kotlin/net/buj/chadasted/Interface.kt` to integrate with your server
3. Do `$ git commit -a` to commit your changed
4. Build the plugin with `$ ./gradlew build`

> Attention!
> 
> This plugin is not meant to be used out-of-the-box. The file modification step can only
> be skipped if you want to test the plugin itself.
> 
> Committing a change to your local repository is necessary if you want to receive updates for
> this plugin in the future.

## Usage

- For players: `/messageformat <format...>`
- For admins: `/messageformatof <player> <format...>`
- For console: `/messageformatof <player> <format...>`

Tags:

- `{name}` - Your colored username
- `{message}` - Your message
- `{rank}` - Your server rank
- `{rmessage}` - Escaped message
- `{wins}` - Your win count
- `{plays}` - The amount of games you've played
- `{playtime}` - Total amount of time you've spent on the server
- `{id}` - Display your ID (IMPORTANT! Do not configure this to display player's user ID as that may lead to account theft)
- A secret tag you'll have to discover for yourself

Your message format must have at least `{name}` and [ `{message}` or `{rmessage}` ].

This plugin is meant to work seamlessly with default formatting options and the inserted tags are to be treated
as uncolored. Any violation of this guarantee is a bug.

## Contributing

All feature requests must have tracking issues opened. All other contributions only require a Pull Request.
This project is licensed under AGPL.
