
## Git

Implemented git like CVS with tests and commands:

* `init` -- init repository
* `add <files>` -- add files
* `rm <files>` -- delete from repository
* `status` -- get current status
* `commit <message>` -- make commit with date and time
* `reset <to_revision>` -- hard reset to revision 
* `log [from_revision]` -- show log
* `checkout <revision>` -- checkout to revision
    * `revision`:
        * `commit hash` 
        * `<branch>` 
        * `HEAD~N`
* `checkout -- <files>` -- reset changes to files

* `branch-create <branch>` -- create branch
* `branch-remove <branch>` -- delete branch
* `show-branches` -- show all branches
* `merge <branch>` -- merge `<branch>` in current branch

`<smth>` -- required argument
`[smth]` -- additional argument

## Command-line interface

Use gitcli to run git: gitcli `<path to repository>` `<command>` `[args]`
