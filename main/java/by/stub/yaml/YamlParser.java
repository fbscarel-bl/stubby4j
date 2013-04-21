/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.stub.yaml;

import by.stub.cli.ANSITerminal;
import by.stub.utils.FileUtils;
import by.stub.utils.ReflectionUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class YamlParser {

   private final static Yaml SNAKE_YAML;

   static {
      SNAKE_YAML = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new YamlParserResolver());
   }

   private static final String YAML_NODE_REQUEST = "request";


   public List<StubHttpLifecycle> parse(final Reader yamlReader) throws Exception {

      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();
      final List<?> loadedYamlData = loadYamlData(yamlReader);

      for (final Object rawParentNode : loadedYamlData) {

         final LinkedHashMap<String, Object> parentNode = (LinkedHashMap<String, Object>) rawParentNode;

         final StubHttpLifecycle parentStub = unmarshallYamlNodeToHttpLifeCycle(parentNode);
         httpLifecycles.add(parentStub);

         reportToConsole(parentStub);
      }

      return httpLifecycles;
   }


   @SuppressWarnings("unchecked")
   protected StubHttpLifecycle unmarshallYamlNodeToHttpLifeCycle(final LinkedHashMap<String, Object> parentNodesMap) throws Exception {

      final StubHttpLifecycle httpLifecycle = new StubHttpLifecycle();

      for (final Map.Entry<String, Object> parentNode : parentNodesMap.entrySet()) {

         final Object parentNodeValue = parentNode.getValue();

         if (parentNodeValue instanceof LinkedHashMap) {
            handleLinkedHashMapNode(httpLifecycle, parentNode);

         } else if (parentNodeValue instanceof ArrayList) {
            handleArrayListNode(httpLifecycle, parentNode);
         }
      }

      return httpLifecycle;
   }

   private void handleLinkedHashMapNode(final StubHttpLifecycle stubHttpLifecycle, final Map.Entry<String, Object> parentNode) throws Exception {

      final LinkedHashMap<String, Object> yamlProperties = (LinkedHashMap<String, Object>) parentNode.getValue();

      if (parentNode.getKey().equals(YAML_NODE_REQUEST)) {
         final StubRequest targetStub = constructStubsFromLinkedHashMap(yamlProperties, StubRequest.class);
         stubHttpLifecycle.setRequest(targetStub);

      } else {
         final StubResponse targetStub = constructStubsFromLinkedHashMap(yamlProperties, StubResponse.class);
         stubHttpLifecycle.setResponse(targetStub);
      }
   }


   @SuppressWarnings("unchecked")
   protected <T> T constructStubsFromLinkedHashMap(final LinkedHashMap<String, Object> yamlProperties, final Class<T> targetStubClass) throws Exception {

      final T targetStub = targetStubClass.newInstance();

      for (final Map.Entry<String, Object> pair : yamlProperties.entrySet()) {

         final Object rawPairValue = pair.getValue();
         final String pairKey = pair.getKey();
         final Object massagedPairValue;

         if (rawPairValue instanceof ArrayList) {
            massagedPairValue = rawPairValue;

         } else if (rawPairValue instanceof Map) {
            massagedPairValue = encodeAuthorizationHeader(rawPairValue);

         } else if (pairKey.toLowerCase().equals("method")) {
            massagedPairValue = new ArrayList<String>(1) {{
               add(pairValueToString(rawPairValue));
            }};

         } else if (pairKey.toLowerCase().equals("file")) {
            massagedPairValue = extractBytesFromFilecontent(rawPairValue);

         } else {
            massagedPairValue = pairValueToString(rawPairValue);
         }

         ReflectionUtils.setPropertyValue(targetStub, pairKey, massagedPairValue);
      }

      return targetStub;
   }

   private void handleArrayListNode(final StubHttpLifecycle stubHttpLifecycle, final Map.Entry<String, Object> parentNode) throws Exception {

      final ArrayList yamlProperties = (ArrayList) parentNode.getValue();
      final List<StubResponse> populatedResponseStub = constructStubsFromArrayList(yamlProperties, StubResponse.class);
      stubHttpLifecycle.setResponse(populatedResponseStub);
   }

   @SuppressWarnings("unchecked")
   private <T> List<T> constructStubsFromArrayList(final ArrayList yamlProperties, final Class<T> targetStubClass) throws Exception {

      final List<T> targetStubList = new LinkedList<T>();
      for (final Object arrayListEntry : yamlProperties) {

         final LinkedHashMap<String, Object> rawSequenceEntry = (LinkedHashMap<String, Object>) arrayListEntry;
         final T targetStub = targetStubClass.newInstance();

         for (final Map.Entry<String, Object> mapEntry : rawSequenceEntry.entrySet()) {
            final String rawSequenceEntryKey = mapEntry.getKey();
            final Object rawSequenceEntryValue = mapEntry.getValue();

            ReflectionUtils.setPropertyValue(targetStub, rawSequenceEntryKey, rawSequenceEntryValue);
         }

         targetStubList.add(targetStub);
      }

      return targetStubList;
   }

   private void reportToConsole(final StubHttpLifecycle parentStub) {
      final ArrayList<String> method = parentStub.getRequest().getMethod();
      final String url = parentStub.getRequest().getUrl();
      final String loadedMsg = String.format("Loaded: %s %s", method, url);
      ANSITerminal.loaded(loadedMsg);
   }

   private byte[] extractBytesFromFilecontent(final Object rawPairValue) throws IOException {

      final String relativeFilePath = pairValueToString(rawPairValue);
      final String extension = StringUtils.extractFilenameExtension(relativeFilePath);

      if (FileUtils.ASCII_TYPES.contains(extension)) {
         return FileUtils.asciiFileToUtf8Bytes(relativeFilePath);
      }

      return FileUtils.binaryFileToBytes(relativeFilePath);
   }

   private String pairValueToString(final Object value) throws IOException {
      final String rawValue = value != null ? value.toString() : "";

      return rawValue.trim();
   }

   protected Map<String, String> encodeAuthorizationHeader(final Object value) {

      final Map<String, String> pairValue = (HashMap<String, String>) value;
      if (!pairValue.containsKey(StubRequest.AUTH_HEADER)) {
         return pairValue;
      }
      final String rawHeader = pairValue.get(StubRequest.AUTH_HEADER);
      final String authorizationHeader = StringUtils.isSet(rawHeader) ? rawHeader.trim() : rawHeader;
      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", StringUtils.encodeBase64(authorizationHeader));
      pairValue.put(StubRequest.AUTH_HEADER, encodedAuthorizationHeader);

      return pairValue;
   }

   protected List<?> loadYamlData(final Reader io) throws IOException {

      final Object loadedYaml = SNAKE_YAML.load(io);

      if (loadedYaml instanceof ArrayList) {
         return (ArrayList<?>) loadedYaml;
      }

      throw new IOException("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");
   }


   private static final class YamlParserResolver extends Resolver {

      public YamlParserResolver() {
         super();
      }

      @Override
      protected void addImplicitResolvers() {
         // no implicit resolvers - resolve everything to String
      }
   }
}