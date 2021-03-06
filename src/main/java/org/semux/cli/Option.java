/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.cli;

/**
 * Option enum for CLI
 */
public enum Option {

    HELP("help"),

    VERSION("version"),

    ACCOUNT("account"),

    CHANGE_PASSWORD("changepassword"),

    DATA_DIR("datadir"),

    COINBASE("coinbase"),

    PASSWORD("password"),

    DUMP_PRIVATE_KEY("dumpprivatekey"),

    IMPORT_PRIVATE_KEY("importprivatekey"),

    NETWORK("network");

    private final String name;

    Option(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
