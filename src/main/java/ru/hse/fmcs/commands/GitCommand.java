package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;

import java.util.List;

public abstract class GitCommand {
    public abstract void run(List<String> arguments) throws GitException;
}
