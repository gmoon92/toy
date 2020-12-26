package com.gmoon.spreadsheet.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleSpreadSheetUtils {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Google Sheets API";
    private static final String TOKENS_DIRECTORY_PATH = "src/test/resources/google/tokens";
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/google/credentials.json";
    private static final int CREDENTIALS_LOCAL_SERVER_PORT = 8888;
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private static Sheets getSheets() throws IOException, GeneralSecurityException {
        final NetHttpTransport googleNetHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(googleNetHttpTransport, JSON_FACTORY, getCredentials(googleNetHttpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static ValueRange getValueRange(String spreadsheetId, String sheetName, String cellRange) throws IOException, GeneralSecurityException {
        final String range = String.format("%s!%s", sheetName, cellRange);
        Sheets sheets = getSheets();

        return sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
    }

    // Build flow and trigger user authorization request.
    private static Credential getCredentials(final NetHttpTransport googleNetHttpTransport) throws IOException {
        GoogleClientSecrets clientSecrets = getClientSecrets();
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(googleNetHttpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(CREDENTIALS_LOCAL_SERVER_PORT)
                .build();
        return new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");
    }

    // Load client secrets.
    private static GoogleClientSecrets getClientSecrets() throws IOException {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        if (in == null)
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);

        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }
}
