package ru.myx.ae3boot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import ru.myx.access.AccessGroupFactory;
import ru.myx.access.AccessUserFactory;
import ru.myx.ae1.control.Control;
import ru.myx.ae1.handle.Handle;
import ru.myx.ae1.know.Know;
import ru.myx.ae1.messaging.Messaging;
import ru.myx.ae1.provide.ProvideRunner;
import ru.myx.ae1evtlog.EventRecieverToStdout;
import ru.myx.ae1evtlog.EvtLogShutdownHook;
import ru.myx.ae1evtlog.EvtState;
import ru.myx.ae1locking.LockManagerFactory;
import ru.myx.ae1runners.RunnerDatabaseTransfer;
import ru.myx.ae1runners.RunnerDatabaseUpdate;
import ru.myx.ae1runners.RunnerScript;
import ru.myx.ae2virtual.FactoryVirtualSource;
import ru.myx.ae3.Engine;
import ru.myx.ae3.act.Act;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.act.CrisisWatcherService;
import ru.myx.ae3.answer.ReplyAnswer;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseList;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitiveString;
import ru.myx.ae3.base.BaseProperty;
import ru.myx.ae3.cache.CacheFactory;
import ru.myx.ae3.control.field.ControlField;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.eval.Evaluate;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.exec.ExecStateCode;
import ru.myx.ae3.exec.ModifierArguments;
import ru.myx.ae3.exec.OperationsA00;
import ru.myx.ae3.exec.OperationsA01;
import ru.myx.ae3.exec.OperationsA10;
import ru.myx.ae3.exec.OperationsA11;
import ru.myx.ae3.exec.OperationsA2X;
import ru.myx.ae3.exec.OperationsA3X;
import ru.myx.ae3.exec.ProgramAssembly;
import ru.myx.ae3.flow.Flow;
import ru.myx.ae3.help.Dom;
import ru.myx.ae3.help.Format;
import ru.myx.ae3.help.Text;
import ru.myx.ae3.know.Guid;
import ru.myx.ae3.l2.LayoutEngine;
import ru.myx.ae3.produce.Produce;
import ru.myx.ae3.reflect.Reflect;
import ru.myx.ae3.report.ReceiverMultiple;
import ru.myx.ae3.report.Report;
import ru.myx.ae3.report.ReportReceiver;
import ru.myx.ae3.status.StatusRegistry;
import ru.myx.ae3.vfs.Entry;
import ru.myx.ae3.vfs.EntryBinary;
import ru.myx.ae3.vfs.EntryContainer;
import ru.myx.ae3.vfs.Storage;
import ru.myx.ae3.vfs.TreeLinkType;
import ru.myx.ae3.vfs.ars.ArsStorage;
import ru.myx.ae3.vfs.status.StorageImplStatus;
import ru.myx.file_control.FileControl;
import ru.myx.geo.GeographyImpl;
import ru.myx.jdbc.lock.Lock;
import ru.myx.renderer.base.RendererBaseMain;
import ru.myx.renderer.ecma.AcmEcmaLanguageImpl;
import ru.myx.renderer.ecma.RendererEcmaMain;
import ru.myx.renderer.map.RendererMapMain;
import ru.myx.renderer.nothing.RendererNullMain;
import ru.myx.renderer.text.RendererTextMain;
import ru.myx.renderer.xslt.RendererXsltMain;
import ru.myx.sapi.CalendarSAPI;
import ru.myx.sapi.ControlSAPI;
import ru.myx.sapi.CreateSAPI;
import ru.myx.sapi.DefaultSAPI;
import ru.myx.sapi.FileSAPI;
import ru.myx.sapi.FlagsSAPI;
import ru.myx.sapi.FormatSAPI;
import ru.myx.sapi.ImagingSAPI;
import ru.myx.sapi.RandomSAPI;
import ru.myx.sapi.ReflectionSAPI;
import ru.myx.sapi.RequestSAPI;
import ru.myx.sapi.SessionSAPI;
import ru.myx.sapi.SortSAPI;
import ru.myx.sapi.UserManagerSAPI;
import ru.myx.sapi.UserSAPI;
import ru.myx.sapi.default_sapi.Function_intl;
import ru.myx.sql.profiler.DriverProfiler;
import ru.myx.util.EntrySimple;
import ru.myx.xstore.CommandCreateFactory;
import ru.myx.xstore.FinderFactory;
import ru.myx.xstore.MessageFactoryVisaAccepted;
import ru.myx.xstore.MessageFactoryVisaDeclined;
import ru.myx.xstore.MessageFactoryVisaVersion;
import ru.myx.xstore.RunnerScriptOnEntry;

/** @author myx */
public final class ThreadBootACM extends Thread {
	
	static final class ExceptionCatcher implements UncaughtExceptionHandler {
		
		Throwable e;

		@Override
		public void uncaughtException(final Thread t, final Throwable e) {
			
			this.e = e;
		}
	}

	private static final int WATCHDOG_TIMEOUT = 300;

	private static final int WATCHDOG_CRITICAL = 30;

	private static final int EXIT_TIMEOUT = 30;

	private static final int EXIT_CRITICAL = 10;

	private static boolean touched = false;

	/**
	 *
	 */
	public static final void init() {
		
		synchronized (ThreadBootACM.class) {
			if (ThreadBootACM.touched) {
				return;
			}
			ThreadBootACM.touched = true;
		}
		System.out.println("BOOT: STARTED: " + ThreadBootACM.WATCHDOG_TIMEOUT + " seconds for initialization...");
		final Thread thread = new ThreadBootACM();
		thread.setDaemon(true);
		final ExceptionCatcher catcher = new ExceptionCatcher();
		thread.setUncaughtExceptionHandler(catcher);
		thread.start();
		try {
			for (int left = ThreadBootACM.WATCHDOG_TIMEOUT - 1; left >= 0; left--) {
				thread.join(1000L);
				if (!thread.isAlive()) {
					if (catcher.e != null) {
						if (catcher.e instanceof Error) {
							throw (Error) catcher.e;
						}
						if (catcher.e instanceof RuntimeException) {
							throw (RuntimeException) catcher.e;
						}
						throw new RuntimeException(catcher.e);
					}
					if (left <= ThreadBootACM.WATCHDOG_CRITICAL) {
						System.out.println("BOOT: WATCHDOG: DONE: Initialization succeed - " + left + " seconds left for initialization not to fail...");
					}
					break;
				}
				if (left <= 20) {
					System.out.println("BOOT: WATCHDOG: " + left + " seconds left for initialization not to fail...");
				}
				if (left == 0) {
					System.out.println("BOOT: WATCHDOG: FATAL: Not initialized in " + ThreadBootACM.WATCHDOG_TIMEOUT + " seconds - system exit");
					System.out.flush();
					final Thread exitThread = new Thread() {
						
						@Override
						public void run() {
							
							Runtime.getRuntime().exit(-2);
						}
					};
					try {
						exitThread.start();
						for (int exitLeft = ThreadBootACM.EXIT_TIMEOUT - 1; exitLeft >= 0; exitLeft--) {
							exitThread.join(1000L);
							if (exitLeft <= ThreadBootACM.EXIT_CRITICAL) {
								System.out.println("BOOT: WATCHDOG: " + exitLeft + " seconds left to exit normally...");
							}
						}
					} catch (final InterruptedException e) {
						return;
					} catch (final Throwable t) {
						t.printStackTrace();
					}
					System.out.println(
							"BOOT: WATCHDOG: FATAL: Not initialized in " + ThreadBootACM.WATCHDOG_TIMEOUT + " seconds nor exited normally in next " + ThreadBootACM.EXIT_TIMEOUT
									+ " seconds - system halt");
					System.out.flush();
					Runtime.getRuntime().halt(-3);
				}
			}
		} catch (final InterruptedException e) {
			// ignore
		}
	}

	private static final void initClass(final Class<?> cls) throws ClassNotFoundException {
		
		final String name = cls.getName();
		System.out.println("BOOT: class: " + name);
		Class.forName(name, true, ThreadBootACM.class.getClassLoader());
	}

	private static final Guid initIdentity(final String key, final File source) {
		
		{
			final Guid parameter = Guid.fromBase64(System.getProperty(key, ""));
			if (parameter != Guid.GUID_NULL) {
				if (parameter.isGuid384()) {
					System.out.println("BOOT: identity read from parameter (key=" + key + ")");
					return parameter;
				}
				System.out.println("BOOT: identity specified in parameter but incorrect (key=" + key + ")");
			}
		}
		if (source != null) {
			try {
				final File file = new File(source, "boot.properties");
				final Properties properties = new Properties();
				if (file.exists()) {
					try (final InputStream input = new FileInputStream(file)) {
						properties.load(input);
					}
				}
				{
					final Guid property = Guid.fromBase64(properties.getProperty(key, ""));
					if (property != Guid.GUID_NULL) {
						if (property.isGuid384()) {
							System.out.println("BOOT: identity read from properties (key=" + key + ")");
							return property;
						}
						System.out.println("BOOT: identity specified in properties but incorrect (key=" + key + ")");
					} else {
						System.out.println("BOOT: identity to be created and stored in properties (key=" + key + ")");
					}
				}
				{
					final Guid generated = Guid.createGuid384();
					System.setProperty(key, generated.toBase64());
					properties.setProperty(key, generated.toBase64());
					try (final OutputStream out = new FileOutputStream(file)) {
						properties.store(out, "identity (" + key + ") automatically generated.");
					}
					return generated;
				}
			} catch (final Throwable t) {
				throw new RuntimeException("fatal initialization exception - seems to be not enough permissions!", t);
			}
		}
		{
			final Guid generated = Guid.createGuid384();
			System.setProperty(key, generated.toBase64());
			return generated;
		}
	}

	private static final void initProperty(final String key, final String valueDefault) {
		
		System.setProperty(key, System.getProperty(key, valueDefault));
	}

	private boolean instanceTouched = false;

	@Override
	public void run() {
		
		synchronized (this) {
			if (this.instanceTouched) {
				throw new IllegalStateException("Second time?");
			}
			this.instanceTouched = true;
		}
		final Runtime runtime = Runtime.getRuntime();
		try {
			ThreadBootACM.initProperty("ru.myx.ae3.properties.path.private", new File(new File(new File(System.getProperty("user.home")), "acm.cm5"), "private").getAbsolutePath());
			ThreadBootACM
					.initProperty("ru.myx.ae3.properties.path.protected", new File(new File(new File(System.getProperty("user.home")), "acm.cm5"), "protected").getAbsolutePath());
			ThreadBootACM.initProperty("ru.myx.ae3.properties.path.public", new File(System.getProperty("user.dir")).getAbsolutePath());

			/** Actually initializes Engine! */
			final Guid guidCluster = ThreadBootACM.initIdentity("ru.myx.ae3.properties.cluster.identity", Engine.PATH_PROTECTED);
			final Guid guidInstance = ThreadBootACM.initIdentity("ru.myx.ae3.properties.instance.identity", Engine.PATH_PRIVATE);
			final Guid guidSession = ThreadBootACM.initIdentity("ru.myx.ae3.properties.session.identity", null);

			/** Initialize report */
			{
				System.out.println("BOOT: Current Report/Event level is " + Report.LEVEL_NAME);
				System.out.println(
						"BOOT: Assertion checks are " + (Report.MODE_ASSERT
							? "ON"
							: "OFF"));
			}

			{
				{
					System.out.println("BOOT: --------------------------------------------------------------");
					System.out.println("BOOT: Java Version: " + System.getProperty("java.version"));
					System.out.println("BOOT: Java Vendor : " + System.getProperty("java.vendor"));
					System.out.println("BOOT: JVM Name    : " + System.getProperty("java.vm.name"));
					System.out.println("BOOT: JVM Version : " + System.getProperty("java.vm.version"));
					System.out.println("BOOT: JVM Vendor  : " + System.getProperty("java.vm.vendor"));
					System.out.println("BOOT: JRE Name    : " + System.getProperty("java.runtime.name"));
					System.out.println("BOOT: JRE Version : " + System.getProperty("java.runtime.version"));
					System.out.println("BOOT: OS Architect: " + System.getProperty("os.arch"));
					System.out.println("BOOT: OS Name     : " + System.getProperty("os.name"));
					System.out.println("BOOT: OS Version  : " + System.getProperty("os.version"));
					System.out.println("BOOT: RT3 Version : " + Know.systemBuild());
					System.out.println("BOOT: cluster     : " + guidCluster);
					System.out.println("BOOT: instance    : " + guidInstance);
					System.out.println("BOOT: session     : " + guidSession);
					System.out.println("BOOT: --------------------------------------------------------------");
				}
				{
					final String guid1 = Engine.createGuid();
					final String guid2 = Engine.GUID_PRODUCER.toString();
					System.out.println("BOOT: Guid check: " + !guid1.equals(guid2) + ", (" + guid1 + " != " + guid2 + ")");
				}
				{
					if (Exec.createProcess(null, "Initialization test") == null) {
						System.out.println("BOOT: AE3 process: FAIL");
						runtime.exit(-4);
					}
				}
				{
					System.out.println("BOOT: Date check: " + Engine.CURRENT_TIME);
				}
				{
					System.out.println("BOOT: Act parallelism: " + Engine.PARALLELISM);
					System.out.println("BOOT: Act peak-load: " + Act.PEAK_LOAD);
				}
				{
					System.out.println("BOOT: EXEC check: MOD count = " + ModifierArguments.values().length);
					System.out.println("BOOT: EXEC check: MOD: " + Arrays.asList(ModifierArguments.values()));
					System.out.println("BOOT: EXEC check: STT count = " + ExecStateCode.values().length);
					System.out.println("BOOT: EXEC check: STT: " + Arrays.asList(ExecStateCode.values()));
					System.out.println("BOOT: EXEC check: A00 count = " + OperationsA00.values().length);
					System.out.println("BOOT: EXEC check: A00: " + Arrays.asList(OperationsA00.values()));
					System.out.println("BOOT: EXEC check: A01 count = " + OperationsA01.values().length);
					System.out.println("BOOT: EXEC check: A01: " + Arrays.asList(OperationsA01.values()));
					System.out.println("BOOT: EXEC check: A10 count = " + OperationsA10.values().length);
					System.out.println("BOOT: EXEC check: A10: " + Arrays.asList(OperationsA10.values()));
					System.out.println("BOOT: EXEC check: A11 count = " + OperationsA11.values().length);
					System.out.println("BOOT: EXEC check: A11: " + Arrays.asList(OperationsA11.values()));
					System.out.println("BOOT: EXEC check: A2X count = " + OperationsA2X.values().length);
					System.out.println("BOOT: EXEC check: A2X: " + Arrays.asList(OperationsA2X.values()));
					System.out.println("BOOT: EXEC check: A3X count = " + OperationsA3X.values().length);
					System.out.println("BOOT: EXEC check: A3X: " + Arrays.asList(OperationsA3X.values()));
				}
				{
					System.out.println("BOOT: Max memory  : " + Format.Compact.toBytes(runtime.maxMemory()));
					System.out.println("BOOT: Total memory: " + Format.Compact.toBytes(runtime.totalMemory()));
					System.out.println("BOOT: Free memory : " + Format.Compact.toBytes(runtime.freeMemory()));
				}
				{
					System.out.println("BOOT: init system classes: start");

					ThreadBootACM.initClass(Text.class);

					ThreadBootACM.initClass(Evaluate.class);
					ThreadBootACM.initClass(Dom.class);
					ThreadBootACM.initClass(Report.class);
					ThreadBootACM.initClass(ReceiverMultiple.class);
					ThreadBootACM.initClass(Reflect.class);
					ThreadBootACM.initClass(Flow.class);
					ThreadBootACM.initClass(ReplyAnswer.class);
					ThreadBootACM.initClass(Reflect.class);
					ThreadBootACM.initClass(LayoutEngine.class);

					ThreadBootACM.initClass(Context.class);

					ThreadBootACM.initClass(CacheFactory.class);
					ThreadBootACM.initClass(Lock.class);
					ThreadBootACM.initClass(Handle.class);
					ThreadBootACM.initClass(Messaging.class);
					ThreadBootACM.initClass(Control.class);
					ThreadBootACM.initClass(ControlField.class);
					ThreadBootACM.initClass(ControlFieldset.class);

					ThreadBootACM.initClass(ImagingSAPI.class);
					ThreadBootACM.initClass(ControlSAPI.class);

				}

				System.out.println("BOOT: init system classes: done");
				{
					System.out.flush();
				}
			}
			final ExecProcess process = Exec.getRootProcess();
			{
				assert process == Exec.currentProcess() : "Must be run in root process!";
			}
			
			final BaseObject GLOBAL = ExecProcess.GLOBAL;
			assert GLOBAL != null : "Global object is NULL!";
			assert !GLOBAL.baseIsPrimitive() : "Global object is primitive!";

			/** ACM specific */
			{
				{
					final ReportReceiver reciever = Report.createReceiver(null);
					final BaseObject reportRegistry = Base.forUnknown(new ReceiverMultiple(reciever));
					GLOBAL.baseDefine("$reportAudit", reportRegistry, BaseProperty.ATTRS_MASK_NNN);
					GLOBAL.baseDefine("$reportLog", reportRegistry, BaseProperty.ATTRS_MASK_NNN);

					final EventRecieverToStdout ets = new EventRecieverToStdout();
					Context.getServer(process).registerEventReciever(ets);
					StatusRegistry.ROOT_REGISTRY.register(new EvtState(ets));
					try {
						runtime.addShutdownHook(new EvtLogShutdownHook());
					} catch (final Throwable t) {
						t.printStackTrace();
					}
				}

				{
					final BaseObject CreateSAPI = new CreateSAPI();
					GLOBAL.baseDefine("Create", CreateSAPI, BaseProperty.ATTRS_MASK_WND);
					GLOBAL.baseDefine("CreateAPI", CreateSAPI, BaseProperty.ATTRS_MASK_NEN);
					assert GLOBAL.baseGet("Create", BaseObject.UNDEFINED) != BaseObject.UNDEFINED : "Must be DEFINED!";
				}
				{
					System.out.println("BOOT: init system global objects: default object");
					final BaseObject DEFAULT = DefaultSAPI.DEFAULT;
					assert Base.hasProperty(DEFAULT, "xmlToMap") : "Default API is expected to have xmlToMap property";
					GLOBAL.baseDefine("Default", DEFAULT, BaseProperty.ATTRS_MASK_WNN);
					GLOBAL.baseDefine("DefaultAPI", DEFAULT, BaseProperty.ATTRS_MASK_NEN);
					GLOBAL.baseDefine("", DEFAULT, BaseProperty.ATTRS_MASK_NNN);
					assert GLOBAL.baseGet("Default", BaseObject.UNDEFINED) == DEFAULT : "Should be equal!";
					assert GLOBAL.baseGet("DefaultAPI", BaseObject.UNDEFINED) == DEFAULT : "Should be equal!";
					assert GLOBAL.baseGet("", BaseObject.UNDEFINED) == DEFAULT : "Should be equal!";

					{
						final Iterator<? extends CharSequence> iterator = DEFAULT.baseKeysOwnAll();
						for (; iterator.hasNext();) {

							final BasePrimitiveString name = Base.forString(iterator.next());

							assert BaseObject.PROTOTYPE.baseGetOwnProperty(name) == null //
							: "Standard methods should not be overridden here";

							assert GLOBAL.baseGetOwnProperty(name) == null //
							: "GLOBAL object is not expected to have property named: " + name;

							GLOBAL.baseDefine(name, DEFAULT.baseGet(name, BaseObject.UNDEFINED), BaseProperty.ATTRS_MASK_NNN);

						}
					}

					{
						for (final Method method : DefaultSAPI.class.getDeclaredMethods()) {

							final String name = method.getName();
							if (BaseObject.PROTOTYPE.baseGet(name, null) != null) {
								/** We don't want to override standard methods here, not in this
								 * case */
								continue;
							}
							final BaseObject value = DEFAULT.baseGet(name, BaseObject.UNDEFINED);

							assert GLOBAL.baseGet(name, null) == null || GLOBAL.baseGet(name, BaseObject.UNDEFINED) == value //
							: "GLOBAL object is not expected to have property named: " + name;

							GLOBAL.baseDefine(name, value, BaseProperty.ATTRS_MASK_NNN);

						}
					}
				}
			}
			
			/** AE3 specific */
			{
				@SuppressWarnings("unchecked")
				final Map.Entry<String, BaseObject>[] entries = new EntrySimple[]{
						new EntrySimple<>("File", Base.forUnknown(new FileSAPI())), //
						new EntrySimple<>("Format", Base.forUnknown(new FormatSAPI())), //
						new EntrySimple<>("Imaging", Base.forUnknown(new ImagingSAPI())), //
						new EntrySimple<>("Random", Base.forUnknown(new RandomSAPI())), //
						new EntrySimple<>("Reflection", Base.forUnknown(new ReflectionSAPI())), //
				};

				for (final Map.Entry<String, BaseObject> sapi : entries) {
					final String simpleName = sapi.getKey();
					final String apiName = simpleName + "API";
					System.out.println("BOOT: init system global object (AE3): " + simpleName + " / " + apiName);

					GLOBAL.baseDefine(simpleName, sapi.getValue(), BaseProperty.ATTRS_MASK_WNN);
					GLOBAL.baseDefine(apiName, sapi.getValue(), BaseProperty.ATTRS_MASK_NEN);

					assert GLOBAL.baseGet(apiName, BaseObject.UNDEFINED) != BaseObject.UNDEFINED //
					: "Must be DEFINED (apiName:" + apiName + ")!";
				}
			}

			assert process == Exec.currentProcess() : "Must be run in root process!";

			{
				GLOBAL.baseDefine("intl", new Function_intl(), BaseProperty.ATTRS_MASK_NNN);
			}

			{
				System.out.println("BOOT: init system global objects: calendar object");
				final BaseObject CALENDAR = Base.forUnknown(new CalendarSAPI());
				GLOBAL.baseDefine("Calendar", CALENDAR, BaseProperty.ATTRS_MASK_WNN);
				GLOBAL.baseDefine("CalendarAPI", CALENDAR, BaseProperty.ATTRS_MASK_NEN);
			}
			{
				System.out.println("BOOT: init system global objects: flags object");
				final BaseObject FLAGS = new FlagsSAPI();
				GLOBAL.baseDefine("Flags", FLAGS, BaseProperty.ATTRS_MASK_WNN);
				GLOBAL.baseDefine("FlagsAPI", FLAGS, BaseProperty.ATTRS_MASK_NEN);
			}
			{
				System.out.println("BOOT: init system global objects: session object");
				final BaseObject SESSION = SessionSAPI.INSTANCE;
				GLOBAL.baseDefine("Session", SESSION, BaseProperty.ATTRS_MASK_WNN);
				GLOBAL.baseDefine("SessionAPI", SESSION, BaseProperty.ATTRS_MASK_NEN);
			}
			{
				System.out.println("BOOT: init system global objects: user manager object");
				final BaseObject userManagerApi = Base.forUnknown(UserManagerSAPI.INSTANCE);
				GLOBAL.baseDefine("UserManager", userManagerApi, BaseProperty.ATTRS_MASK_WND);
				GLOBAL.baseDefine("UserManagerAPI", userManagerApi, BaseProperty.ATTRS_MASK_NEN);
			}
			{
				System.out.println("BOOT: init system global objects: user object");
				final BaseObject USER = Base.forUnknown(UserSAPI.INSTANCE);
				GLOBAL.baseDefine("User", USER, BaseProperty.ATTRS_MASK_WNN);
				GLOBAL.baseDefine("UserAPI", USER, BaseProperty.ATTRS_MASK_NEN);
			}
			{
				System.out.println("BOOT: init system global objects: request object");
				final BaseObject REQUEST = RequestSAPI.INSTANCE;
				GLOBAL.baseDefine("Request", REQUEST, BaseProperty.ATTRS_MASK_WNN);
				GLOBAL.baseDefine("RequestAPI", REQUEST, BaseProperty.ATTRS_MASK_NEN);
			}
			{
				GLOBAL.baseDefine("Sort", Base.forUnknown(new SortSAPI()));
			}
			{
				final BaseObject ctrl = Base.forUnknown(ControlSAPI.INSTANCE);
				GLOBAL.baseDefine("Admin", ctrl, BaseProperty.ATTRS_MASK_WNN);
				GLOBAL.baseDefine("ControlAPI", ctrl, BaseProperty.ATTRS_MASK_NEN);
			}
			{
				assert Base.forUnknown(System.class).baseGet("out", null) != null : "Reflector failure!";
				assert Base.forUnknown(System.class).baseGet("2ut", null) == null : "Reflector failure!";
				assert Base.forUnknown(System.class).baseGet("out", BaseObject.UNDEFINED) != BaseObject.UNDEFINED : "Reflector failure!";
			}
			{
				final Entry global = Storage.PUBLIC.relative("resources/global.js", null);
				if (global == null) {
					System.out.println("BOOT: init system global.js: not found");
				} else {
					final EntryBinary binary = global.toBinary();

					System.out.println("BOOT: init system global.js: " + Format.Compact.toBytes(binary.getBinaryContentLength()) + "bytes");

					final ExecProcess ctx = Exec.createProcess(process, "public global.js initialisation context");

					final ProgramAssembly assembly = new ProgramAssembly(ctx);
					Evaluate.compileProgramInline(
							AcmEcmaLanguageImpl.INSTANCE,
							"public, global.js initialisation script",
							binary.getBinaryContent().baseValue().toString(),
							assembly);

					final Throwable error = Act.run(ctx, new Runnable() {

						@Override
						public void run() {
							
							ctx.vmScopeDeriveContext(GLOBAL);
							assembly.toProgram(0).callVE0(ctx, GLOBAL);
						}
					});
					
					if (error != null) {
						error.printStackTrace();
						runtime.exit(-1);
						return;
					}
				}
			}

			{
				Produce.registerFactory(new AccessUserFactory());
				Produce.registerFactory(new AccessGroupFactory());
			}

			{
				// FIXME: move to ImplementVfs when it will exist
				System.out.println("BOOT: initialize VFS storage mount");

				try {
					Storage.mount(Storage.getRoot(null), "status", TreeLinkType.PUBLIC_TREE_REFERENCE, Storage.createRoot(new StorageImplStatus(StatusRegistry.ROOT_REGISTRY)));
				} catch (final Throwable e) {
					e.printStackTrace();
					runtime.exit(-1);
				}

				try {
					final String storage = System.getProperty("ru.myx.ae3.properties.vfs.storage", "s4fs:lcl:bdbje");

					final BaseList<Object> constants = BaseObject.createArray(2);
					constants.baseDefaultPush(Base.forString("ru.myx.ae3.sys/vfs/VfsStorageFactory"));
					constants.baseDefaultPush(Base.forString(storage));

					final ExecProcess ctx = Exec.createProcess(process, "Main storage initialization");
					ctx.vmScopeDeriveContext(ExecProcess.GLOBAL);

					final Object o = Evaluate.evaluateObject("require( @0 ).create( @1 )", ctx, constants).baseValue();

					if (!(o instanceof ArsStorage)) {
						throw new IllegalArgumentException(
								"Not a storage. Storage: " + storage + ", class: " + (o == null
									? "null"
									: o.getClass().getName()) + ", object: " + Format.Describe.toEcmaSource(o, ""));
					}

					final EntryContainer root = Storage.createRoot((ArsStorage<?, ?, ?>) o);

					Storage.mount(Storage.getRoot(null), "storage", TreeLinkType.PUBLIC_TREE_REFERENCE, root);
				} catch (final Throwable e) {
					e.printStackTrace();
					runtime.exit(-1);
				}
			}

			System.out.println("BOOT: initialize transform");
			{
				LayoutEngine.getDocumentation();
				ru.myx.ae3.transform.mime.Main.main(null);
				ru.myx.ae3.transform.smp.Main.main(null);
			}
			System.out.println("BOOT: initialize geography");
			{
				GeographyImpl.main(null);
			}
			System.out.println("BOOT: init system global objects and factories: done");
			{
				try {
					DriverManager.registerDriver(new DriverProfiler());
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
			{
				Produce.registerFactory(new FactoryVirtualSource());
			}
			{
				try {
					FileControl.main(null);
				} catch (final Throwable e) {
					e.printStackTrace();
				}
			}
			{
				ru.myx.ae3.i3.web.http.Main.main(null);
				ru.myx.ae3.i3.web.telnet.Main.main(null);
				ru.myx.ae3.transfer.nio.Main.main(null);
				ru.myx.ae3.transfer.tls.Main.main(null);
			}
			{
				System.out.println("BOOT: plugin: ACM [ACM.ECMA] is being initialized...");
				RendererEcmaMain.main(null);
				System.out.println("BOOT: plugin: ACM [ACM.XSLT] is being initialized...");
				RendererXsltMain.main(null);
				System.out.println("BOOT: plugin: ACM [ACM.MAP] is being initialized...");
				RendererMapMain.main(null);
				System.out.println("BOOT: plugin: ACM [ACM.BASE] is being initialized...");
				RendererBaseMain.main(null);
				System.out.println("BOOT: plugin: ACM [ACM.TEXT] is being initialized...");
				RendererTextMain.main(null);
				System.out.println("BOOT: plugin: ACM [ACM.NULL] is being initialized...");
				RendererNullMain.main(null);
				System.out.println("BOOT: renderers done.");
			}
			{
				Produce.registerFactory(new CommandCreateFactory());
				Produce.registerFactory(new MessageFactoryVisaVersion());
				Produce.registerFactory(new MessageFactoryVisaAccepted());
				Produce.registerFactory(new MessageFactoryVisaDeclined());
				Produce.registerFactory(new FinderFactory());
				ProvideRunner.register("XDS_SCRIPT", new RunnerScriptOnEntry());
			}
			{
				Lock.managerFactoryImpl(new LockManagerFactory());
				Lock.managerLock();
			}
			{
				ProvideRunner.register("Script", new RunnerScript());
				ProvideRunner.register("DatabaseTransfer", new RunnerDatabaseTransfer());
				ProvideRunner.register("DatabaseUpdate", new RunnerDatabaseUpdate());
			}

			CrisisWatcherService.checkStart();
			System.out.println("BOOT: done.");
		} catch (final Throwable t) {
			System.err.println(">>>>>>>>>> EXCEPTION IN BOOT THREAD");
			t.printStackTrace();
			runtime.exit(-44);
		}
	}
}
